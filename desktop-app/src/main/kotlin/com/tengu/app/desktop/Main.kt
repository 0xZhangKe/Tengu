package com.tengu.app.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    if (args.contains("--cli-client")) {
        val prompt = args.dropWhile { it != "--cli-client" }.drop(1).joinToString(" ").ifBlank {
            "Hello from ACP client. Please reply with one short sentence."
        }
        runBlocking {
            runCliClient(this, prompt)
        }
    } else {
        startDesktopApp()
    }
}

private fun startDesktopApp() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Tengu",
        ) {
            val coroutineScope = rememberCoroutineScope()
            val session = remember { createCodexChatSession(coroutineScope) }
            val chatState by session.state.collectAsState()
            var input by remember { mutableStateOf("Please introduce yourself in one sentence.") }

            DisposableEffect(Unit) {
                onDispose {
                    session.close()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {
                Text("Codex ACP Client")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                    Text(chatState.status)
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Prompt") },
                    enabled = !chatState.busy,
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    chatState.messages.forEach { message ->
                        Text("${message.role.label}: ${message.text}")
                    }
                }
            }
        }
    }
}

private suspend fun runCliClient(
    coroutineScope: CoroutineScope,
    prompt: String,
) {
    val session = createCodexChatSession(coroutineScope)
    try {
        val result = session.send(prompt)
        println(result.reply)
        println("[done] ${result.stopReason}")
    } finally {
        session.close()
    }
}
