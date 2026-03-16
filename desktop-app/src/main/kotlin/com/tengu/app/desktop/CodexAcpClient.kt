@file:OptIn(com.agentclientprotocol.annotations.UnstableApi::class)

package com.tengu.app.desktop

import com.agentclientprotocol.client.Client
import com.agentclientprotocol.client.ClientInfo
import com.agentclientprotocol.client.ClientOperationsFactory
import com.agentclientprotocol.client.ClientSession
import com.agentclientprotocol.common.ClientSessionOperations
import com.agentclientprotocol.common.Event
import com.agentclientprotocol.common.FileSystemOperations
import com.agentclientprotocol.common.SessionCreationParameters
import com.agentclientprotocol.model.AcpCreatedSessionResponse
import com.agentclientprotocol.model.ClientCapabilities
import com.agentclientprotocol.model.ContentBlock
import com.agentclientprotocol.model.FileSystemCapability
import com.agentclientprotocol.model.PermissionOption
import com.agentclientprotocol.model.PermissionOptionKind
import com.agentclientprotocol.model.ReadTextFileResponse
import com.agentclientprotocol.model.RequestPermissionOutcome
import com.agentclientprotocol.model.RequestPermissionResponse
import com.agentclientprotocol.model.ModelId
import com.agentclientprotocol.model.SessionId
import com.agentclientprotocol.model.SessionUpdate
import com.agentclientprotocol.model.StopReason
import com.agentclientprotocol.model.WriteTextFileResponse
import com.agentclientprotocol.protocol.Protocol
import com.agentclientprotocol.transport.StdioTransport
import com.tengu.app.common.ui.model.ChatMessageRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonElement
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.time.Duration.Companion.seconds

data class CodexChatMessage(
    val role: CodexChatRole,
    val text: String,
)

enum class CodexChatRole(val label: String) {
    USER("You"),
    ASSISTANT("Codex"),
}

fun CodexChatRole.convert(): ChatMessageRole {
    return when (this) {
        CodexChatRole.USER -> ChatMessageRole.USER
        CodexChatRole.ASSISTANT -> ChatMessageRole.ASSISTANT
    }
}

data class CodexChatState(
    val connected: Boolean = false,
    val busy: Boolean = false,
    val status: String = "Disconnected",
    val messages: List<CodexChatMessage> = emptyList(),
    val availableModels: List<CodexModelInfo> = emptyList(),
    val currentModelId: String? = null,
)

data class CodexChatTurn(
    val prompt: String,
    val reply: String,
    val stopReason: StopReason,
)

data class CodexModelInfo(
    val id: String,
    val name: String,
    val description: String? = null,
)

interface CodexChatSession : AutoCloseable {

    val projectDir: String

    val state: StateFlow<CodexChatState>

    suspend fun start()

    suspend fun send(prompt: String): CodexChatTurn

    suspend fun getAvailableModels(): List<CodexModelInfo>

    suspend fun setModel(modelId: String)
}

fun createCodexChatSession(
    coroutineScope: CoroutineScope,
    projectDir: String,
    initialModel: String? = null,
): CodexChatSession = DefaultCodexChatSession(
    coroutineScope = coroutineScope,
    projectDir = projectDir,
    initialModel = initialModel,
)

private class DefaultCodexChatSession(
    private val coroutineScope: CoroutineScope,
    override val projectDir: String,
    private val initialModel: String?,
) : CodexChatSession {

    private val connectionMutex = Mutex()
    private val promptMutex = Mutex()

    private val _state = MutableStateFlow(CodexChatState())
    override val state: StateFlow<CodexChatState> = _state.asStateFlow()

    private var process: Process? = null
    private var session: ClientSession? = null
    private var activeAssistantIndex: Int? = null
    private var modelStateJob: Job? = null

    override suspend fun start() {
        connectionMutex.withLock {
            if (session != null) {
                syncModelState(checkNotNull(session))
                updateState(connected = true, status = "Ready")
                return
            }

            updateState(connected = false, busy = true, status = "Launching codex-acp...")
            val launchedProcess = launchCodexAcp(projectDir, initialModel)
            streamAgentErrors(launchedProcess)

            val transport = StdioTransport(
                parentScope = coroutineScope,
                input = launchedProcess.inputStream.asSource().buffered(),
                output = launchedProcess.outputStream.asSink().buffered(),
                ioDispatcher = Dispatchers.IO,
            )
            val protocol = Protocol(coroutineScope, transport)
            val client = Client(protocol)

            try {
                protocol.start()
                updateState(connected = false, busy = true, status = "Initializing ACP client...")
                val agentInfo = withTimeout(20.seconds) {
                    client.initialize(
                        ClientInfo(
                            capabilities = ClientCapabilities(
                                fs = FileSystemCapability(readTextFile = true, writeTextFile = true)
                            )
                        )
                    )
                }

                val createdSession = withTimeout(20.seconds) {
                    client.newSession(
                        SessionCreationParameters(
                            cwd = projectDir,
                            mcpServers = emptyList()
                        ),
                        operationsFactory = DesktopClientSupport(projectDir, ::updateStatus),
                    )
                }

                process = launchedProcess
                session = createdSession
                observeModelState(createdSession)
                syncModelState(createdSession)
                updateState(
                    connected = true,
                    busy = false,
                    status = buildReadyStatus(agentInfo.protocolVersion)
                )
            } catch (t: Throwable) {
                destroyProcess(launchedProcess)
                updateState(
                    connected = false,
                    busy = false,
                    status = "Connect failed: ${t.message ?: t::class.simpleName}"
                )
                throw t
            }
        }
    }

    override suspend fun send(prompt: String): CodexChatTurn {
        require(prompt.isNotBlank()) { "Prompt cannot be blank" }
        start()

        return promptMutex.withLock {
            val currentSession = checkNotNull(session) { "ACP session is not ready" }
            activeAssistantIndex = null
            appendMessage(CodexChatMessage(CodexChatRole.USER, prompt))
            updateState(connected = true, busy = true, status = "Waiting for Codex reply...")

            val assistantReply = StringBuilder()
            var stopReason = StopReason.END_TURN

            try {
                currentSession.prompt(listOf(ContentBlock.Text(prompt))).collect { event ->
                    when (event) {
                        is Event.SessionUpdateEvent -> handleSessionUpdate(event.update, assistantReply)
                        is Event.PromptResponseEvent -> stopReason = event.response.stopReason
                    }
                }
            } catch (t: Throwable) {
                activeAssistantIndex = null
                updateState(connected = true, busy = false, status = "Send failed: ${t.message ?: t::class.simpleName}")
                throw t
            }

            activeAssistantIndex = null
            updateState(
                connected = true,
                busy = false,
                status = "Turn finished: $stopReason"
            )
            CodexChatTurn(prompt, assistantReply.toString(), stopReason)
        }
    }

    override suspend fun getAvailableModels(): List<CodexModelInfo> {
        start()
        val currentSession = checkNotNull(session) { "ACP session is not ready" }
        return if (!currentSession.modelsSupported) {
            emptyList()
        } else {
            currentSession.availableModels.map { it.convert() }
        }
    }

    override suspend fun setModel(modelId: String) {
        require(modelId.isNotBlank()) { "Model id cannot be blank" }
        start()
        val currentSession = checkNotNull(session) { "ACP session is not ready" }
        check(currentSession.modelsSupported) { "This ACP session does not support model selection" }

        updateState(connected = true, busy = true, status = "Switching model to $modelId...")
        try {
            currentSession.setModel(ModelId(modelId))
            syncModelState(currentSession)
            updateState(
                connected = true,
                busy = false,
                status = "Ready (model: ${currentSession.currentModel.value.value})"
            )
        } catch (t: Throwable) {
            syncModelState(currentSession)
            updateState(
                connected = true,
                busy = false,
                status = "Set model failed: ${t.message ?: t::class.simpleName}"
            )
            throw t
        }
    }

    override fun close() {
        modelStateJob?.cancel()
        modelStateJob = null
        destroyProcess(process)
        process = null
        session = null
        activeAssistantIndex = null
        _state.update { current ->
            current.copy(
                connected = false,
                busy = false,
                status = "Disconnected",
                availableModels = emptyList(),
                currentModelId = null,
            )
        }
    }

    private fun streamAgentErrors(process: Process) {
        coroutineScope.launch(Dispatchers.IO) {
            process.errorStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (isCodexAcpDebugEnabled()) {
                        System.err.println("[codex-acp] $line")
                    }
                }
            }
        }
    }

    private fun observeModelState(session: ClientSession) {
        if (!session.modelsSupported) {
            return
        }
        modelStateJob?.cancel()
        modelStateJob = coroutineScope.launch {
            session.currentModel.collect {
                syncModelState(session)
            }
        }
    }

    private fun syncModelState(session: ClientSession) {
        val availableModels = if (session.modelsSupported) {
            session.availableModels.map { it.convert() }
        } else {
            emptyList()
        }
        val currentModelId = if (session.modelsSupported) {
            session.currentModel.value.value
        } else {
            null
        }
        _state.update { current ->
            current.copy(
                availableModels = availableModels,
                currentModelId = currentModelId,
            )
        }
    }

    private fun buildReadyStatus(protocolVersion: Int): String {
        val currentModelId = state.value.currentModelId
        return if (currentModelId.isNullOrBlank()) {
            "Ready ($protocolVersion)"
        } else {
            "Ready ($protocolVersion, model: $currentModelId)"
        }
    }

    private fun handleSessionUpdate(
        update: SessionUpdate,
        assistantReply: StringBuilder,
    ) {
        when (update) {
            is SessionUpdate.AgentMessageChunk -> {
                val text = renderContent(update.content)
                if (text.isNotEmpty()) {
                    assistantReply.append(text)
                    appendAssistantChunk(text)
                    updateStatus("Receiving reply...")
                }
            }

            is SessionUpdate.AgentThoughtChunk -> {
                if (renderContent(update.content).isNotEmpty()) {
                    updateStatus("Reasoning...")
                }
            }

            is SessionUpdate.ToolCall -> {
                updateStatus("Tool: ${update.title}${renderStatus(update.status?.name)}")
            }

            is SessionUpdate.ToolCallUpdate -> {
                val title = update.title ?: update.toolCallId.value
                updateStatus("Tool: $title${renderStatus(update.status?.name)}")
            }

            is SessionUpdate.PlanUpdate -> {
                val entries = update.entries.joinToString(" | ") { entry -> "${entry.status}: ${entry.content}" }
                updateStatus("Plan: $entries")
            }

            else -> updateStatus("Working...")
        }
    }

    private fun appendMessage(message: CodexChatMessage) {
        _state.update { current ->
            current.copy(messages = current.messages + message)
        }
    }

    private fun appendAssistantChunk(chunk: String) {
        _state.update { current ->
            val messages = current.messages.toMutableList()
            val currentIndex = activeAssistantIndex
            if (currentIndex == null) {
                messages.add(CodexChatMessage(CodexChatRole.ASSISTANT, chunk))
                activeAssistantIndex = messages.lastIndex
            } else {
                val existing = messages[currentIndex]
                messages[currentIndex] = existing.copy(text = existing.text + chunk)
            }
            current.copy(messages = messages)
        }
    }

    private fun updateStatus(status: String) {
        _state.update { current ->
            current.copy(status = status)
        }
    }

    private fun updateState(
        connected: Boolean = _state.value.connected,
        busy: Boolean = _state.value.busy,
        status: String = _state.value.status,
    ) {
        _state.update { current ->
            current.copy(
                connected = connected,
                busy = busy,
                status = status,
            )
        }
    }

    private fun renderStatus(status: String?): String {
        return if (status.isNullOrBlank()) "" else " [$status]"
    }

    private fun renderContent(content: ContentBlock): String {
        return when (content) {
            is ContentBlock.Text -> content.text
            is ContentBlock.ResourceLink -> content.title ?: content.name
            is ContentBlock.Resource -> "[resource]"
            is ContentBlock.Image -> "[image:${content.mimeType}]"
            is ContentBlock.Audio -> "[audio:${content.mimeType}]"
        }
    }
}

private fun com.agentclientprotocol.model.ModelInfo.convert(): CodexModelInfo {
    return CodexModelInfo(
        id = modelId.value,
        name = name,
        description = description,
    )
}

private class DesktopClientSupport(
    private val projectDir: String,
    private val updateStatus: (String) -> Unit,
) : ClientOperationsFactory {

    override suspend fun createClientOperations(
        sessionId: SessionId,
        sessionResponse: AcpCreatedSessionResponse
    ): ClientSessionOperations = DesktopClientSession(projectDir, updateStatus)
}

private class DesktopClientSession(
    private val projectDir: String,
    private val updateStatus: (String) -> Unit,
) : ClientSessionOperations, FileSystemOperations {

    override suspend fun requestPermissions(
        toolCall: SessionUpdate.ToolCallUpdate,
        permissions: List<PermissionOption>,
        _meta: JsonElement?
    ): RequestPermissionResponse {
        val selected = permissions.firstOrNull {
            it.kind == PermissionOptionKind.ALLOW_ONCE || it.kind == PermissionOptionKind.ALLOW_ALWAYS
        } ?: permissions.first()
        updateStatus("Permission: ${toolCall.title ?: toolCall.toolCallId.value} -> ${selected.name}")
        return RequestPermissionResponse(RequestPermissionOutcome.Selected(selected.optionId))
    }

    override suspend fun notify(notification: SessionUpdate, _meta: JsonElement?) {
        when (notification) {
            is SessionUpdate.AvailableCommandsUpdate -> updateStatus("Commands ready")
            else -> Unit
        }
    }

    override suspend fun fsReadTextFile(
        path: String,
        line: UInt?,
        limit: UInt?,
        _meta: JsonElement?
    ): ReadTextFileResponse {
        val file = File(projectDir)
        return ReadTextFileResponse(file.readText())
    }

    override suspend fun fsWriteTextFile(
        path: String,
        content: String,
        _meta: JsonElement?
    ): WriteTextFileResponse {
        val file = File(path)
        file.writeText(content)
        return WriteTextFileResponse()
    }
}

private fun launchCodexAcp(projectRoot: String, initialModel: String?): Process {
    val executable = resolveCodexAcpExecutable()
    val command = mutableListOf(
        executable.toString(),
        "-c",
        "shell_environment_policy.inherit=all",
    )
    if (!initialModel.isNullOrBlank()) {
        command += listOf("-c", "model=${escapeTomlString(initialModel)}")
    }
    return ProcessBuilder(command)
        .directory(File(projectRoot))
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
}

private fun escapeTomlString(value: String): String {
    return buildString {
        append('"')
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }
}

private fun resolveCodexAcpExecutable(): Path {
    val configured = System.getenv("CODEX_ACP_BIN")?.takeIf { it.isNotBlank() }?.let { Path.of(it) }
    if (configured != null && configured.exists()) {
        return configured
    }

    val candidates = listOf(
        Path.of("/opt/homebrew/bin/codex-acp"),
        Path.of("/usr/local/bin/codex-acp"),
    )
    return candidates.firstOrNull { it.exists() } ?: Path.of("codex-acp")
}

private fun destroyProcess(process: Process?) {
    if (process == null) {
        return
    }
    runCatching { process.outputStream.close() }
    runCatching { process.inputStream.close() }
    runCatching { process.errorStream.close() }
    process.destroy()
    if (process.isAlive) {
        process.destroyForcibly()
    }
}

private fun isCodexAcpDebugEnabled(): Boolean {
    return System.getenv("CODEX_ACP_DEBUG") == "1" || System.getProperty("codex.acp.debug") == "true"
}
