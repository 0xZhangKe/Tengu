package com.tengu.app.desktop

import com.agentclientprotocol.common.Event
import com.agentclientprotocol.model.ContentBlock
import com.agentclientprotocol.model.PromptResponse
import com.agentclientprotocol.model.SessionUpdate
import com.agentclientprotocol.model.StopReason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.nio.file.Paths

/**
 * Runs one Codex CLI request and streams terminal output as ACP events.
 */
fun codexCliPromptFlow(codexWorkingDirectory: String, prompt: String): Flow<Event> = flow {
    val process = runCatching {
        ProcessBuilder("codex", "exec", prompt)
            .directory(Paths.get(codexWorkingDirectory).toFile())
            .redirectErrorStream(true)
            .start()
    }.getOrElse { throwable ->
        emit(
            Event.SessionUpdateEvent(
                SessionUpdate.AgentMessageChunk(
                    ContentBlock.Text("failed to start codex: ${throwable.message ?: throwable::class.simpleName}")
                )
            )
        )
        emit(Event.PromptResponseEvent(PromptResponse(stopReason = StopReason.END_TURN)))
        return@flow
    }

    try {
        process.inputStream.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                emit(
                    Event.SessionUpdateEvent(
                        SessionUpdate.AgentMessageChunk(ContentBlock.Text(line))
                    )
                )
            }
        }

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            emit(
                Event.SessionUpdateEvent(
                    SessionUpdate.AgentMessageChunk(
                        ContentBlock.Text("codex process exited with code $exitCode")
                    )
                )
            )
        }
    } finally {
        if (process.isAlive) {
            process.destroyForcibly()
        }
        emit(Event.PromptResponseEvent(PromptResponse(stopReason = StopReason.END_TURN)))
    }
}
