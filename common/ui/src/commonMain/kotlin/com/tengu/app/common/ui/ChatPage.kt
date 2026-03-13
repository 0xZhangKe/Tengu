package com.tengu.app.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    messageList: List<ChatMessage>,
    onSendMessageClick: (String) -> Unit,
    connected: Boolean,
) {
    var inputText by remember { mutableStateOf("") }
    val density = LocalDensity.current

    SubcomposeLayout(
        modifier = modifier,
    ) { constraints ->
        val inputPlaceables = subcompose(ChatPageSlot.InputBar) {
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
            measurable.measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = 0,
                ),
            )
        }

        val inputHeight = inputPlaceables.maxOfOrNull { it.height } ?: 0
        val bottomPadding = with(density) { inputHeight.toDp() } + 16.dp

        val contentPlaceables = subcompose(ChatPageSlot.Content) {
            ChatMessageList(
                messageList = messageList,
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = bottomPadding,
                ),
            )
        }.map { measurable ->
            measurable.measure(
                constraints.copy(
                    minWidth = 0,
                    minHeight = 0,
                ),
            )
        }

        val layoutWidth = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            maxOf(
                contentPlaceables.maxOfOrNull { it.width } ?: 0,
                inputPlaceables.maxOfOrNull { it.width } ?: 0,
            )
        }
        val layoutHeight = if (constraints.hasBoundedHeight) {
            constraints.maxHeight
        } else {
            maxOf(
                contentPlaceables.maxOfOrNull { it.height } ?: 0,
                inputHeight,
            )
        }

        layout(layoutWidth, layoutHeight) {
            contentPlaceables.forEach { placeable ->
                placeable.placeRelative(0, 0)
            }
            val inputY = layoutHeight - inputHeight
            inputPlaceables.forEach { placeable ->
                placeable.placeRelative(0, inputY)
            }
        }
    }
}

private enum class ChatPageSlot {
    Content,
    InputBar,
}

@Composable
private fun ChatInputBar(
    inputText: String,
    connected: Boolean,
    onInputTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            TextField(
                modifier = Modifier.heightIn(min = 82.dp).fillMaxWidth(),
                value = inputText,
                onValueChange = onInputTextChange,
                colors = TextFieldDefaults.colors(
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                placeholder = {
                    Text(text = "Type anything...")
                },
                minLines = 1,
                maxLines = 4,
            )
            Button(
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = onSendClick,
            ) {
                Text(text = "Send")
            }
        }
    }
}
