package com.tengu.app.desktop

import com.agentclientprotocol.agent.AgentSession
import com.agentclientprotocol.agent.client
import com.agentclientprotocol.agent.clientInfo
import com.agentclientprotocol.annotations.UnstableApi
import com.agentclientprotocol.common.Event
import com.agentclientprotocol.model.ContentBlock
import com.agentclientprotocol.model.PromptResponse
import com.agentclientprotocol.model.SessionId
import com.agentclientprotocol.model.SessionUpdate
import com.agentclientprotocol.model.StopReason
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonElement

// 1. Build a dedicated AgentSession implementation for each connection.
class TerminalAgentSession(
    override val sessionId: SessionId
) : AgentSession {
    @OptIn(UnstableApi::class)
    override suspend fun prompt(
        content: List<ContentBlock>,
        _meta: JsonElement?
    ): Flow<Event> = flow {
        // Echo back what the user typed.
        val userText = content.filterIsInstance<ContentBlock.Text>().joinToString(" ") { it.text }
        emit(
            Event.SessionUpdateEvent(
                SessionUpdate.AgentMessageChunk(ContentBlock.Text("Agent heard: $userText"))
            )
        )

        // Optional extension call via the coroutine context.
        val context = currentCoroutineContext()
        val clientCapabilities = context.clientInfo.capabilities
        if (clientCapabilities.fs?.readTextFile == true) {
            val fs = context.client
            val readmeSnippet = fs.fsReadTextFile("../README.md").content.take(120)
            emit(
                Event.SessionUpdateEvent(
                    SessionUpdate.AgentMessageChunk(ContentBlock.Text("README preview: $readmeSnippet…"))
                )
            )
        }

        // Finish the turn once updates are sent.
        emit(Event.PromptResponseEvent(PromptResponse(StopReason.END_TURN)))
    }

    override suspend fun cancel() {
        // No long-running work in this demo, so nothing to clean up yet.
    }
}
