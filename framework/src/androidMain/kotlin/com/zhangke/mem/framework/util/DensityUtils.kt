package com.tengu.app.framework.util

import android.content.Context
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

fun Dp.dpToPx(context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (this.value * density + 0.5f).roundToInt()
}
