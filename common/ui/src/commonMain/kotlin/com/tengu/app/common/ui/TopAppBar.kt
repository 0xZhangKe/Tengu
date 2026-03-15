package com.tengu.app.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.tengu.app.framework.theme.TenguTheme

@Composable
fun TopAppBar(
    modifier: Modifier,
    title: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .height(42.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        TenguTheme.colorScheme.surface,
                        TenguTheme.colorScheme.surface,
                        TenguTheme.colorScheme.surface,
                        TenguTheme.colorScheme.surface,
                        TenguTheme.colorScheme.surface,
                        TenguTheme.colorScheme.surface,
                        TenguTheme.colorScheme.surface.copy(alpha = 0.6F),
                        TenguTheme.colorScheme.surface.copy(alpha = 0.5F),
                        TenguTheme.colorScheme.surface.copy(alpha = 0.1F),
                    ),
                )
            ),
    ) {
        Box(modifier = Modifier.align(Alignment.Center)) {
            CompositionLocalProvider(
                LocalTextStyle provides TenguTheme.typography.labelMedium,
            ) {
                title()
            }
        }
    }
}
