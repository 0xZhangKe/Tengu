package com.tengu.app.framework.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.compositionLocalOf

data class TenguColorScheme(
    val materialColorScheme: ColorScheme,
) {

    val primary get() = materialColorScheme.primary
    val onPrimary get() = materialColorScheme.onPrimary
    val background get() = materialColorScheme.background
    val surface get() = materialColorScheme.surface
    val onSurface get() = materialColorScheme.onSurface
    val onSurfaceVariant get() = materialColorScheme.onSurfaceVariant
    val tertiary get() = materialColorScheme.tertiary
    val onTertiary get() = materialColorScheme.onTertiary
    val inverseSurface get() = materialColorScheme.inverseSurface
    val inverseOnSurface get() = materialColorScheme.inverseOnSurface

    val secondary get() = materialColorScheme.secondary

    val surfaceContainerHigh get() = materialColorScheme.surfaceContainerHigh

    val surfaceContainerHighest get() = materialColorScheme.surfaceContainerHighest

    val outline get() = materialColorScheme.outline
    val outlineVariant get() = materialColorScheme.outlineVariant

    companion object {

        fun fromColorScheme(colorScheme: ColorScheme): TenguColorScheme {
            return TenguColorScheme(colorScheme)
        }
    }
}

val LocalTenguColorScheme =
    compositionLocalOf<TenguColorScheme> { throw IllegalStateException("No TenguColorScheme provided") }
