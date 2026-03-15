package com.tengu.app.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tengu.app.common.ui.model.ChatMessage

@Composable
fun ChatMessageList(
    modifier: Modifier = Modifier,
    messageList: List<ChatMessage>,
    contentPadding: PaddingValues,
) {
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    var keepScrolledToBottom by remember { mutableStateOf(true) }
    val latestMessageKey = remember(messageList) {
        messageList.lastOrNull()?.let { message ->
            when (message) {
                is ChatMessage.Text -> "${message.createAt}-${message.role}-${message.text.length}"
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrolledToBottom() }
            .collect { isScrolledToBottom ->
                keepScrolledToBottom = isScrolledToBottom
            }
    }

    LaunchedEffect(messageList.size, latestMessageKey) {
        if (keepScrolledToBottom && messageList.isNotEmpty()) {
            listState.scrollToItem(messageList.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
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

private fun LazyListState.isScrolledToBottom(): Boolean {
    val layoutInfo = layoutInfo
    val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return true
    val lastItemIndex = layoutInfo.totalItemsCount - 1
    if (lastItemIndex < 0) {
        return true
    }
    return lastVisibleItem.index == lastItemIndex &&
        lastVisibleItem.offset + lastVisibleItem.size <= layoutInfo.viewportEndOffset
}
