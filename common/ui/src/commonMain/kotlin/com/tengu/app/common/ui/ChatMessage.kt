package com.tengu.app.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
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
    val contentColor = TenguTheme.colorScheme.materialColorScheme.contentColorFor(bubbleColor)
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
                Markdown(
                    modifier = Modifier,
                    content = message.text,
                    colors = defaultMarkdownColors(
                        text = contentColor,
                    ),
                    typography = defaultMarkdownTypography(),
                )
            }
        }
    }
}

@Composable
fun defaultMarkdownColors(
    text: Color = MaterialTheme.colorScheme.onSurface,
): MarkdownColors {
    return markdownColor(
        text = text,
        codeBackground = TenguTheme.colorScheme.surfaceContainerHighest,
        inlineCodeBackground = TenguTheme.colorScheme.surfaceContainerHighest,
    )
}

@Composable
fun defaultMarkdownTypography(): MarkdownTypography {
    return markdownTypography(
        text = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
        ),
        paragraph = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 12.sp,
        ),
        code = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
        ),
        inlineCode = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
        ),
    )
}

