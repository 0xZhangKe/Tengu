package com.tengu.app.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.tengu.app.common.ui.model.ChatMessage
import com.tengu.app.framework.theme.TenguTheme
import com.tengu.app.framework.utils.dpToPx
import com.tengu.app.framework.utils.pxToDp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    messageList: List<ChatMessage>,
    connected: Boolean,
    title: String,
    onSendMessageClick: (String) -> Unit,
    onTitleClick: () -> Unit,
) {
    var inputText by remember { mutableStateOf("") }
    val density = LocalDensity.current

    SubcomposeLayout(
        modifier = modifier,
    ) { constraints ->
        val titlePlaceable = subcompose(ChatPageSlot.Title) {
            TopAppBar(
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onTitleClick,
                ) {
                    Text(
                        text = title,
                        color = TenguTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }.first()
        val inputPlaceable = subcompose(ChatPageSlot.InputBar) {
            ChatInputBar(
                inputText = inputText,
                connected = connected,
                onInputTextChange = { inputText = it },
                onSendClick = {
                    val message = inputText.trim()
                    if (message.isEmpty()) {
                        return@ChatInputBar
                    }
                    onSendMessageClick(message)
                    inputText = ""
                },
            )
        }.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }.first()

        val inputHeight = inputPlaceable.height
        val bottomPadding = with(density) { inputHeight.toDp() } + 32.dp

        val contentPlaceable = subcompose(ChatPageSlot.Content) {
            ChatMessageList(
                messageList = messageList,
                contentPadding = PaddingValues(
                    top = titlePlaceable.height.pxToDp(density) + 16.dp,
                    bottom = bottomPadding,
                ),
            )
        }.map { measurable ->
            measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }.first()

        val layoutWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            contentPlaceable.width
        }
        val layoutHeight = if (constraints.hasBoundedHeight) {
            constraints.maxHeight
        } else {
            contentPlaceable.height
        }

        layout(layoutWidth, layoutHeight) {
            contentPlaceable.placeRelative(0, 0)
            val inputY = layoutHeight - inputHeight - 16.dpToPx(density).roundToInt()
            inputPlaceable.placeRelative(0, inputY)
            titlePlaceable.placeRelative(
                x = layoutWidth / 2 - titlePlaceable.width / 2,
                y = 0,
            )
        }
    }
}

private enum class ChatPageSlot {
    Content,
    InputBar,
    Title,
}

@Composable
private fun ChatInputBar(
    inputText: String,
    connected: Boolean,
    onInputTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
//        shadowElevation = 1.dp,
        shape = TenguTheme.shapes.medium,
        border = BorderStroke(width = 0.5.dp, color = TenguTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp),
                value = inputText,
                onValueChange = onInputTextChange,
                colors = TextFieldDefaults.colors(
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                textStyle = TenguTheme.typography.bodySmall,
                placeholder = {
                    Text(
                        text = "Type anything...",
                        style = TenguTheme.typography.bodySmall,
                    )
                },
                minLines = 1,
                maxLines = 5,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1F))
                TextButton(
                    modifier = Modifier,
                    onClick = onSendClick,
                ) {
                    Text(text = "Send")
                }
            }
        }
    }
}
