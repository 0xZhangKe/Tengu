package com.tengu.app.desktop.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tengu.app.common.ui.model.ChatMessage
import com.tengu.app.desktop.CodexChatMessage
import com.tengu.app.desktop.CodexChatSession
import com.tengu.app.desktop.convert
import com.tengu.app.desktop.createCodexChatSession
import com.tengu.app.framework.utils.launchInViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.UUID
import kotlin.time.Clock

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState.default())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var session: CodexChatSession? = null

    fun onDirSelected(path: String) {
        _uiState.update { it.copy(path = path) }
        launchInViewModel {
            getSession().start()
        }
    }

    private fun getSession(): CodexChatSession {
        session?.close()
        val session = session
        if (session != null) return session
        val path = if (_uiState.value.path.isNullOrEmpty()) {
            createTemporaryDir()
        } else {
            _uiState.value.path
        }
        return createCodexChatSession(viewModelScope, path!!).also {
            this.session = it
            addCloseable(it)
            observeSessionState(it)
        }
    }

    private fun observeSessionState(session: CodexChatSession) {
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
            getSession().send(message)
        }
    }

    private fun createTemporaryDir(): String {
        val userHome = System.getProperty("user.home")
        val dialogueDir = "$userHome/.tengu/dialogue/"
        val folderName = UUID.randomUUID().toString()
        val dir = File(dialogueDir, folderName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir.absolutePath
    }

    private fun CodexChatMessage.convert(): ChatMessage {
        return ChatMessage.Text(
            role = this.role.convert(),
            text = this.text,
            createAt = Clock.System.now(),
        )
    }
}
