package com.tengu.app.desktop

import com.agentclientprotocol.client.Client
import com.agentclientprotocol.client.ClientInfo
import com.agentclientprotocol.client.ClientOperationsFactory
import com.agentclientprotocol.common.ClientSessionOperations
import com.agentclientprotocol.common.Event
import com.agentclientprotocol.common.FileSystemOperations
import com.agentclientprotocol.common.SessionCreationParameters
import com.agentclientprotocol.model.AcpCreatedSessionResponse
import com.agentclientprotocol.model.ClientCapabilities
import com.agentclientprotocol.model.ContentBlock
import com.agentclientprotocol.model.FileSystemCapability
import com.agentclientprotocol.model.PermissionOption
import com.agentclientprotocol.model.ReadTextFileResponse
import com.agentclientprotocol.model.RequestPermissionOutcome
import com.agentclientprotocol.model.RequestPermissionResponse
import com.agentclientprotocol.model.SessionId
import com.agentclientprotocol.model.SessionUpdate
import com.agentclientprotocol.model.WriteTextFileResponse
import com.agentclientprotocol.protocol.Protocol
import com.agentclientprotocol.transport.StdioTransport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonElement
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.Duration.Companion.seconds


private class TerminalClientSupport(private val projectDir: Path) : ClientOperationsFactory {

    override suspend fun createClientOperations(
        sessionId: SessionId,
        sessionResponse: AcpCreatedSessionResponse
    ): ClientSessionOperations = TerminalSession(projectDir)
}

private class TerminalSession(
    private val projectDir: Path
) : ClientSessionOperations, FileSystemOperations {
    override suspend fun requestPermissions(
        toolCall: SessionUpdate.ToolCallUpdate,
        permissions: List<PermissionOption>,
        _meta: JsonElement?
    ): RequestPermissionResponse =
        // Grant whichever option was first in the list (swap for real UX).
        RequestPermissionResponse(RequestPermissionOutcome.Selected(permissions.first().optionId))

    override suspend fun notify(notification: SessionUpdate, _meta: JsonElement?) {
        // Surface streaming updates back to the host application.
        println("Agent update: $notification")
    }

    override suspend fun fsReadTextFile(
        path: String,
        line: UInt?,
        limit: UInt?,
        _meta: JsonElement?
    ): ReadTextFileResponse =
        // Resolve file paths relative to the workspace root the client chose.
        ReadTextFileResponse(projectDir.resolve(path).readText())

    override suspend fun fsWriteTextFile(
        path: String,
        content: String,
        _meta: JsonElement?
    ): WriteTextFileResponse {
        // Allow the agent to write files through the same extension API.
        projectDir.resolve(path).writeText(content)
        return WriteTextFileResponse()
    }
}

suspend fun startClient(
    coroutineScope: CoroutineScope,
    onStatus: (String) -> Unit,
) {
    val projectRoot = Paths.get("").toAbsolutePath()
    onStatus("Launching embedded ACP agent...")
    val process = launchEmbeddedAgent(projectRoot)
    coroutineScope.launch(Dispatchers.IO) {
        process.errorStream.bufferedReader().useLines { lines ->
            lines.forEach { line -> System.err.println("[agent] $line") }
        }
    }

    val transport = StdioTransport(
        parentScope = coroutineScope,
        input = process.inputStream.asSource().buffered(),
        output = process.outputStream.asSink().buffered(),
        ioDispatcher = Dispatchers.IO,
    )
    val protocol = Protocol(coroutineScope, transport)
    val client = Client(
        protocol = protocol,
    )

    try {
        protocol.start()
        onStatus("Initializing ACP client...")
        val agentInfo = withTimeout(10.seconds) {
            client.initialize(
                ClientInfo(
                    capabilities = ClientCapabilities(
                        fs = FileSystemCapability(readTextFile = true, writeTextFile = true)
                    )
                )
            )
        }
        onStatus("Agent initialized")

        val session = withTimeout(10.seconds) {
            client.newSession(
                SessionCreationParameters(
                    cwd = projectRoot.toString(),
                    mcpServers = emptyList()
                ),
                operationsFactory = TerminalClientSupport(projectRoot),
            )
        }
        onStatus("Session created: ${session.sessionId.value}")

        session.prompt(listOf(ContentBlock.Text("Hello agent!"))).collect { event ->
            when (event) {
                is Event.SessionUpdateEvent -> onStatus("Agent update: ${event.update}")
                is Event.PromptResponseEvent -> onStatus("Prompt finished: ${event.response.stopReason}")
            }
        }
    } catch (t: Throwable) {
        onStatus("ACP failed: ${t.message ?: t::class.simpleName}")
    } finally {
        runCatching { process.outputStream.close() }
        runCatching { process.inputStream.close() }
        runCatching { process.errorStream.close() }
        process.destroy()
        if (process.isAlive) {
            process.destroyForcibly()
        }
    }
}

private fun launchEmbeddedAgent(projectRoot: Path): Process {
    val javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString()
    val classpath = System.getProperty("java.class.path")
    val mainClass = "com.tengu.app.desktop.MainKt"
    return ProcessBuilder(javaBin, "-cp", classpath, mainClass, "--agent")
        .directory(projectRoot.toFile())
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()
}
