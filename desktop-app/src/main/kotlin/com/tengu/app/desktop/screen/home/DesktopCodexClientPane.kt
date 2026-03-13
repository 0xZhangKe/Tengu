package com.tengu.app.desktop.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tengu.app.desktop.createCodexChatSession
import kotlinx.coroutines.launch

@Composable
fun DesktopCodexClientPane(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val session = remember { createCodexChatSession(coroutineScope) }
    val chatState by session.state.collectAsState()
    var input by remember { mutableStateOf("Please introduce yourself in one sentence.") }

    DisposableEffect(session) {
        onDispose {
            session.close()
        }
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = "Codex ACP Client",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    enabled = !chatState.connected && !chatState.busy,
                    onClick = {
                        coroutineScope.launch {
                            runCatching { session.start() }
                        }
                    },
                ) {
                    Text(if (chatState.connected) "Connected" else "Connect")
                }
                Text(
                    text = chatState.status,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = input,
                onValueChange = { input = it },
                label = { Text("Prompt") },
                enabled = !chatState.busy,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                enabled = input.isNotBlank() && !chatState.busy,
                onClick = {
                    coroutineScope.launch {
                        runCatching { session.send(input) }
                    }
                },
            ) {
                Text("Send")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                chatState.messages.forEach { message ->
                    Text(
                        text = "${message.role.label}: ${message.text}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
