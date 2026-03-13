package com.tengu.app.desktop.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.tengu.app.common.ui.ChatPage
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object HomeNavKey : NavKey

@Composable
fun DesktopDrawerScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    DesktopDrawerPage(
        uiState = uiState,
        content = {
            HomePageContent(
                uiState = uiState,
                onSendMessageClick = viewModel::onSendMessageClick,
            )
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DesktopDrawerPage(
    uiState: HomeUiState,
    content: @Composable () -> Unit,
) {
    var isDrawerOpen by rememberSaveable { mutableStateOf(true) }
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                modifier = Modifier.align(Alignment.TopCenter),
                fontSize = 12.sp,
                text = buildString {
                    append(if (uiState.connected) "Connected" else "Disconnected")
                    append(" - ")
                    append(uiState.status)
                },
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                var defaultDrawerWidth by rememberSaveable { mutableStateOf<Dp?>(null) }
                if (defaultDrawerWidth == null && maxWidth > 0.dp) {
                    SideEffect {
                        defaultDrawerWidth = (maxWidth * 0.24f).coerceIn(280.dp, 360.dp)
                    }
                }
                val resolvedDrawerWidth = defaultDrawerWidth ?: 320.dp

                SubcomposeLayout(
                    modifier = Modifier.fillMaxSize(),
                ) { constraints ->
                    val drawerWidth = if (isDrawerOpen) resolvedDrawerWidth else 0.dp
                    val drawerWidthPx = drawerWidth.roundToPx()
                    val contentPadding = PaddingValues(
                        start = drawerWidth + 16.dp,
                        top = 24.dp,
                        end = 24.dp,
                        bottom = 24.dp,
                    )

                    val contentPlaceables = subcompose(HomeLayoutSlot.Content) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding),
                        ) {
                            content()
                        }
                    }.map { measurable ->
                        measurable.measure(constraints)
                    }

                    val drawerPlaceables = if (isDrawerOpen) {
                        subcompose(HomeLayoutSlot.Drawer) {
                            Surface(
                                modifier = Modifier
                                    .width(drawerWidth)
                                    .fillMaxHeight(),
                                tonalElevation = 2.dp,
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 20.dp),
                                ) {
                                    Text(
                                        text = "Panels",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    DesktopDrawerList(
                                        uiState = uiState,
                                    )
                                }
                            }
                        }.map { measurable ->
                            measurable.measure(
                                Constraints(
                                    minWidth = drawerWidthPx,
                                    maxWidth = drawerWidthPx,
                                    minHeight = constraints.minHeight,
                                    maxHeight = constraints.maxHeight,
                                )
                            )
                        }
                    } else {
                        emptyList()
                    }

                    layout(constraints.maxWidth, constraints.maxHeight) {
                        contentPlaceables.forEach { it.place(0, 0) }
                        drawerPlaceables.forEach { it.place(0, 0) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopDrawerList(
    uiState: HomeUiState,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(3) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "item: $it",
            )
        }
    }
}

@Composable
private fun HomePageContent(
    uiState: HomeUiState,
    onSendMessageClick: (String) -> Unit,
) {
    ChatPage(
        modifier = Modifier.fillMaxSize(),
        messageList = uiState.messages,
        connected = uiState.connected,
        onSendMessageClick = onSendMessageClick,
    )
}

private enum class HomeLayoutSlot {
    Drawer,
    Content,
}
