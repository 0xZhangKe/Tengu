package com.tengu.app.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
    val bubbleColor = if (isUser) {
        TenguTheme.colorScheme.primary
    } else {
        TenguTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (isUser) {
        TenguTheme.colorScheme.onPrimary
    } else {
        TenguTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .align(if (isUser) Alignment.CenterEnd else Alignment.CenterStart)
                .widthIn(max = 320.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(if (isUser) Alignment.BottomEnd else Alignment.BottomStart)
                    .offset(x = if (isUser) 4.dp else (-4).dp, y = (-2).dp),
            ) {
                BubbleTail(
                    color = bubbleColor,
                    pointsToRight = isUser,
                )
            }
            Box(
                modifier = Modifier
                    .padding(
                        start = if (isUser) 0.dp else 10.dp,
                        end = if (isUser) 10.dp else 0.dp,
                    )
                    .background(
                        color = bubbleColor,
                        shape = RoundedCornerShape(20.dp),
                    )
                    .padding(vertical = 10.dp, horizontal = 14.dp),
            ) {
                Text(
                    text = message.text,
                    color = contentColor,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun BubbleTail(
    color: Color,
    pointsToRight: Boolean,
) {
    Box(
        modifier = Modifier
            .size(width = 12.dp, height = 14.dp)
            .drawBehind {
                val path = Path().apply {
                    if (pointsToRight) {
                        moveTo(0f, size.height)
                        lineTo(size.width, size.height)
                        lineTo(size.width, 0f)
                    } else {
                        moveTo(0f, 0f)
                        lineTo(0f, size.height)
                        lineTo(size.width, size.height)
                    }
                    close()
                }
                drawPath(path = path, color = color)
            },
    )
}
