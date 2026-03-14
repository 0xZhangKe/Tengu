package com.tengu.app.desktop.composable

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.window.WindowScope

val LocalWindowScope = compositionLocalOf<WindowScope> {
    throw IllegalStateException("WindowScope is not provided!")
}
