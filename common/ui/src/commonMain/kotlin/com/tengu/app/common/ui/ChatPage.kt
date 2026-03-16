package com.tengu.app.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.tengu.app.common.ui.model.ChatMessage
import com.tengu.app.framework.theme.TenguTheme
import com.tengu.app.framework.utils.dpToPx
import com.tengu.app.framework.utils.noRippleClick
import com.tengu.app.framework.utils.pxToDp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    messageList: List<ChatMessage>,
    connected: Boolean,
    title: String,
    modelName: String,
    branchName: String?,
    availableModels: List<String>,
    onSelectModelClick: (String) -> Unit,
    onSendMessageClick: (String) -> Unit,
    onTitleClick: () -> Unit,
    onBranchClick: () -> Unit,
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
                modelName = modelName,
                branchName = branchName,
                availableModels = availableModels,
                onSelectModelClick = onSelectModelClick,
                onInputTextChange = { inputText = it },
                onBranchClick = onBranchClick,
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
    modelName: String,
    branchName: String?,
    availableModels: List<String>,
    onSelectModelClick: (String) -> Unit,
    onInputTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBranchClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        shape = TenguTheme.shapes.medium,
        border = BorderStroke(width = 0.5.dp, color = TenguTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val textFieldState = rememberTextFieldState()
            LaunchedEffect(textFieldState.text) {
                onInputTextChange(textFieldState.text.toString())
            }
            BasicTextField(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                            if (event.isShiftPressed) {
                                false
                            } else {
                                onSendClick()
                                true
                            }
                        } else {
                            false
                        }
                    },
                value = inputText,
                onValueChange = onInputTextChange,
                textStyle = TenguTheme.typography.bodySmall,
                minLines = 3,
                maxLines = 5,
                decorationBox = { innerTextField ->
                    Box {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Type anything...",
                                style = TenguTheme.typography.bodySmall,
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!branchName.isNullOrEmpty()) {
                    Text(
                        modifier = Modifier.noRippleClick { onBranchClick() },
                        text = branchName,
                        style = TenguTheme.typography.labelMedium,
                    )
                }
                var selectorExpanded by remember { mutableStateOf(false) }
                DropdownMenu(
                    modifier = Modifier,
                    expanded = selectorExpanded,
                    onDismissRequest = { selectorExpanded = false },
                ) {
                    for (model in availableModels) {
                        DropdownMenuItem(
                            text = { Text(text = model) },
                            onClick = {
                                selectorExpanded = false
                                onSelectModelClick(model)
                            },
                        )
                    }
                }
                Text(
                    modifier = Modifier.noRippleClick(onClick = { selectorExpanded = true }),
                    text = modelName,
                    style = TenguTheme.typography.labelMedium,
                )
            }
        }
    }
}
