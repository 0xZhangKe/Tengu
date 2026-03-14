package com.tengu.app.desktop.screen.home

import com.tengu.app.common.ui.model.ChatMessage

data class HomeUiState(
    val connected: Boolean,
    val status: String,
    val path: String?,
    val messages: List<ChatMessage>,
) {

    companion object {

        fun default(): HomeUiState {
            return HomeUiState(
                connected = false,
                status = "",
                path = null,
                messages = emptyList(),
            )
        }
    }
}
