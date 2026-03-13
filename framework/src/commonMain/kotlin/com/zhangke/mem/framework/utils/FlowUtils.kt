package com.tengu.app.framework.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> ConsumeFlow(
    flow: Flow<T>,
    block: suspend (T) -> Unit
) {
    val updatedBlock by rememberUpdatedState(block)
    LaunchedEffect(flow) {
        flow.collect {
            updatedBlock(it)
        }
    }
}
