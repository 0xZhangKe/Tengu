package com.tengu.app.framework.nav

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

@Composable
fun BackHandler(enabled: Boolean = true, block: () -> Unit) {
    val navState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = navState,
        isBackEnabled = enabled,
        onBackCancelled = {},
        onBackCompleted = block,
    )
}
