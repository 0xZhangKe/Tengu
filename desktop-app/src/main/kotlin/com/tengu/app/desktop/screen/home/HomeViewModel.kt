package com.tengu.app.desktop.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tengu.app.common.ui.model.ChatMessage
import com.tengu.app.desktop.CodexChatMessage
import com.tengu.app.desktop.CodexChatSession
import com.tengu.app.desktop.convert
import com.tengu.app.desktop.createCodexChatSession
import com.tengu.app.framework.utils.Log
import com.tengu.app.framework.utils.launchInViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import java.io.File
import java.util.UUID
import kotlin.time.Clock

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState.default())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var session: CodexChatSession? = null
    private var sessionPath: String? = null
    private var sessionStateJob: Job? = null

    fun onDirSelected(path: String) {
        resetSession()
        _uiState.update {
            it.copy(
                connected = false,
                status = "",
                path = path,
                messages = emptyList(),
            )
        }
        launchInViewModel {
            getSession().start()
        }
    }

    private fun getSession(): CodexChatSession {
        val requestedPath = _uiState.value.path.takeUnless { it.isNullOrEmpty() }
            ?: sessionPath
            ?: createTemporaryDir()

        session?.takeIf { sessionPath == requestedPath }?.let { existingSession ->
            return existingSession
        }

        resetSession()
        return createCodexChatSession(viewModelScope, requestedPath).also {
            this.session = it
            this.sessionPath = requestedPath
            addCloseable(it)
            observeSessionState(it)
        }
    }

    private fun observeSessionState(session: CodexChatSession) {
        sessionStateJob?.cancel()
        sessionStateJob = launchInViewModel {
            session.state.collect { state ->
                println(state.status)
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

    private fun resetSession() {
        sessionStateJob?.cancel()
        sessionStateJob = null
        session?.close()
        session = null
        sessionPath = null
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
