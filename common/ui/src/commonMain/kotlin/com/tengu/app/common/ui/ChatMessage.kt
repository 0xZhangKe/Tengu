package com.tengu.app.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tengu.app.common.ui.model.ChatMessage
import com.tengu.app.common.ui.model.ChatMessageRole
import com.tengu.app.framework.theme.TenguTheme

@Composable
fun ChatMessage(
    modifier: Modifier = Modifier,
    message: ChatMessage,
) {
    when (message) {
        is ChatMessage.Text -> TextMessage(modifier, message)
    }
}

@Composable
private fun TextMessage(
    modifier: Modifier,
    message: ChatMessage.Text,
) {
    val isUser = message.role == ChatMessageRole.USER
    val bubbleColor = TenguTheme.colorScheme.surfaceContainerHigh
    val contentColor = TenguTheme.colorScheme.onSurface

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7F)
                .align(if (isUser) Alignment.CenterEnd else Alignment.CenterStart)
        ) {
            Box(
                modifier = Modifier
                    .align(if (isUser) Alignment.CenterEnd else Alignment.CenterStart)
                    .padding(
                        start = if (isUser) 0.dp else 8.dp,
                        end = if (isUser) 8.dp else 0.dp,
                    )
                    .background(
                        color = bubbleColor,
                        shape = TenguTheme.shapes.large,
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp),
            ) {
                Text(
                    text = message.text,
                    color = contentColor,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
