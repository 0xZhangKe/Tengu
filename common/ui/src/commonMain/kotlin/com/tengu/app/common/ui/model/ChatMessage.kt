package com.tengu.app.common.ui.model

import kotlin.time.Instant

sealed interface ChatMessage {

    val createAt: Instant

    val role: ChatMessageRole

    data class Text(
        val text: String,
        override val createAt: Instant,
        override val role: ChatMessageRole,
    ) : ChatMessage
}
