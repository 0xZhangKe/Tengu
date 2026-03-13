package com.tengu.app.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tengu.app.common.ui.model.ChatMessage

@Composable
fun ChatMessageList(
    modifier: Modifier = Modifier,
    messageList: List<ChatMessage>,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = contentPadding,
    ) {
        itemsIndexed(
            items = messageList,
            key = { index, message -> "${message.createAt.epochSeconds}-${message.createAt.nanosecondsOfSecond}-$index" },
        ) { _, message ->
            ChatMessage(message = message)
        }
    }
}
