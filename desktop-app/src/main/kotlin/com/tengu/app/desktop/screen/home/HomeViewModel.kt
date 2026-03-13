package com.tengu.app.desktop.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tengu.app.common.ui.model.ChatMessage
import com.tengu.app.desktop.CodexChatMessage
import com.tengu.app.desktop.convert
import com.tengu.app.desktop.createCodexChatSession
import com.tengu.app.framework.utils.launchInViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.file.Path
import kotlin.time.Clock

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState.default())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val session = createCodexChatSession(
        coroutineScope = viewModelScope,
        projectDir = Path.of("./"),
    ).also { addCloseable(it) }

    init {
        launchInViewModel {
            session.start()
        }
        launchInViewModel {
            session.state.collect { state ->
                _uiState.update {
                    it.copy(
                        connected = state.connected,
                        status = state.status,
                        messages = state.messages.map { message -> message.convert() },
                    )
                }
            }
        }
    }

    fun onSendMessageClick(message: String) {
        launchInViewModel {
            session.send(message)
        }
    }

    private fun CodexChatMessage.convert(): ChatMessage {
        return ChatMessage.Text(
            role = this.role.convert(),
            text = this.text,
            createAt = Clock.System.now(),
        )
    }
}
