package com.tengu.app.desktop.screen.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.tengu.app.common.ui.ChatPage
import com.tengu.app.framework.utils.ifNullOrEmpty
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

@Serializable
data object HomeNavKey : NavKey

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HomePage(
        uiState = uiState,
        onSendMessageClick = viewModel::onSendMessageClick,
        onTitleClick = {
            selectDirectory(uiState.path)?.let(viewModel::onDirSelected)
        },
        onSelectModelClick = viewModel::onSelectModelClick,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomePage(
    uiState: HomeUiState,
    onSendMessageClick: (String) -> Unit,
    onTitleClick: () -> Unit,
    onSelectModelClick: (String) -> Unit,
) {
    HomePageScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Text(
                modifier = Modifier.align(Alignment.Center).padding(top = 16.dp),
                text = "Tengu",
            )
        },
        drawer = {
            HomePageDrawer()
        },
    ) {
        HomeSurface(
            modifier = Modifier.fillMaxSize()
        ) {
            HomePageContent(
                uiState = uiState,
                onSendMessageClick = onSendMessageClick,
                onTitleClick = onTitleClick,
                onSelectModelClick = onSelectModelClick,
            )
        }
    }
}

@Composable
private fun HomePageContent(
    uiState: HomeUiState,
    onSendMessageClick: (String) -> Unit,
    onTitleClick: () -> Unit,
    onSelectModelClick: (String) -> Unit,
) {
    ChatPage(
        modifier = Modifier.fillMaxSize(),
        messageList = uiState.messages,
        connected = uiState.connected,
        title = uiState.path.ifNullOrEmpty { "Temporary Dialogue" },
        onTitleClick = onTitleClick,
        availableModels = uiState.availableModelNames,
        modelName = uiState.currentModel?.name.ifNullOrEmpty { "UNKNOWN" },
        onSelectModelClick = onSelectModelClick,
        onSendMessageClick = onSendMessageClick,
    )
}

private fun selectDirectory(currentPath: String?): String? {
    return if (isMacOs()) {
        selectDirectoryWithNativeDialog(currentPath)
    } else {
        selectDirectoryWithChooser(currentPath)
    }
}

private fun selectDirectoryWithNativeDialog(currentPath: String?): String? {
    val previous = System.getProperty("apple.awt.fileDialogForDirectories")
    val dialog = FileDialog(null as Frame?, "Select Folder", FileDialog.LOAD)
    return try {
        System.setProperty("apple.awt.fileDialogForDirectories", "true")
        dialog.directory = currentPath?.takeIf { it.isNotBlank() } ?: System.getProperty("user.home")
        dialog.isVisible = true

        val directory = dialog.directory ?: return null
        val file = dialog.file
        if (file.isNullOrBlank()) {
            File(directory).absolutePath
        } else {
            File(directory, file).absolutePath
        }
    } finally {
        dialog.dispose()
        if (previous == null) {
            System.clearProperty("apple.awt.fileDialogForDirectories")
        } else {
            System.setProperty("apple.awt.fileDialogForDirectories", previous)
        }
    }
}

private fun selectDirectoryWithChooser(currentPath: String?): String? {
    val chooser = JFileChooser(currentPath ?: System.getProperty("user.home")).apply {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        isAcceptAllFileFilterUsed = false
    }
    val result = chooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile?.absolutePath
    } else {
        null
    }
}

private fun isMacOs(): Boolean {
    return System.getProperty("os.name").startsWith("Mac", ignoreCase = true)
}
