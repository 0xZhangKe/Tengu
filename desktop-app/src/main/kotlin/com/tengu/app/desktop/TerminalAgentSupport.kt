package com.tengu.app.desktop

import com.agentclientprotocol.agent.AgentInfo
import com.agentclientprotocol.agent.AgentSession
import com.agentclientprotocol.agent.AgentSupport
import com.agentclientprotocol.client.ClientInfo
import com.agentclientprotocol.common.SessionParameters
import com.agentclientprotocol.model.AgentCapabilities
import com.agentclientprotocol.model.LATEST_PROTOCOL_VERSION
import com.agentclientprotocol.model.SessionId

class TerminalAgentSupport : AgentSupport {
    override suspend fun initialize(clientInfo: ClientInfo) = AgentInfo(
        protocolVersion = LATEST_PROTOCOL_VERSION,
        capabilities = AgentCapabilities() // advertise baseline agent features
    )

    override suspend fun createSession(sessionParameters: SessionParameters): AgentSession {
        // 3. Instantiate the session implementation defined above.
        val sessionId = SessionId("session-${System.currentTimeMillis()}")
        return TerminalAgentSession(sessionId)
    }

    override suspend fun loadSession(sessionId: SessionId, sessionParameters: SessionParameters): AgentSession =
        // Rehydrate existing sessions with the provided identifier.
        TerminalAgentSession(sessionId)
}
