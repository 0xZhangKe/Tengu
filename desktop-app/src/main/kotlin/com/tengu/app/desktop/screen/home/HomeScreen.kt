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
        onTitleClick = {},
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomePage(
    uiState: HomeUiState,
    onSendMessageClick: (String) -> Unit,
    onTitleClick: () -> Unit,
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
            )
        }
    }
}

@Composable
private fun HomePageContent(
    uiState: HomeUiState,
    onSendMessageClick: (String) -> Unit,
    onTitleClick: () -> Unit,
) {
    ChatPage(
        modifier = Modifier.fillMaxSize(),
        messageList = uiState.messages,
        connected = uiState.connected,
        title = uiState.path.ifNullOrEmpty { "Temporary Dialogue" },
        onTitleClick = onTitleClick,
        onSendMessageClick = onSendMessageClick,
    )
}
