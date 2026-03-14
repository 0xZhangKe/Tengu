package com.tengu.app.common.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
            TextButton(
                onClick = onTitleClick,
            ) {
                Text(
                    text = title,
                    style = TenguTheme.typography.titleMedium,
                )
            }
        }.map { it.measure(constraints) }.first()
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
            measurable.measure(constraints)
        }.first()

        val inputHeight = inputPlaceable.height
        val bottomPadding = with(density) { inputHeight.toDp() } + 16.dp

        val contentPlaceable = subcompose(ChatPageSlot.Content) {
            ChatMessageList(
                messageList = messageList,
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = bottomPadding,
                ),
            )
        }.map { measurable ->
            measurable.measure(constraints)
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
            val inputY = layoutHeight - inputHeight - 8.dpToPx(density).roundToInt()
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 16.dp, top = 8.dp, end = 16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            draggedElevation = 2.dp,
            focusedElevation = 2.dp,
            hoveredElevation = 2.dp,
            pressedElevation = 2.dp,
            disabledElevation = 2.dp,
        ),
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
            TextButton(
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = onSendClick,
            ) {
                Text(text = "Send")
            }
        }
    }
}
