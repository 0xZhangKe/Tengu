package com.tengu.app.desktop.screen.home

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tengu.app.framework.theme.TenguTheme

@Composable
fun HomeSurface(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = TenguTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        shape = TenguTheme.shapes.medium,
        content = content,
    )
}
