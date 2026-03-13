package com.tengu.app.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
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
import com.agentclientprotocol.agent.Agent
import com.agentclientprotocol.protocol.Protocol
import com.agentclientprotocol.transport.StdioTransport
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

fun main(args: Array<String>) {
    if (args.contains("--agent")) {
        runBlocking {
            startAgent(this)
            awaitCancellation()
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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                var output by remember { mutableStateOf("") }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            startClient(coroutineScope) { output = it }
                        }
                    },
                ) {
                    Text("Start ACP Session")
                }

                Text(
                    modifier = Modifier.padding(16.dp),
                    text = output,
                )
            }
        }
    }
}

private fun startAgent(coroutineScope: CoroutineScope) {
    val transport = StdioTransport(
        parentScope = coroutineScope,
        input = System.`in`.asSource().buffered(),
        output = System.out.asSink().buffered(),
        ioDispatcher = Dispatchers.IO,
    )
    val protocol = Protocol(coroutineScope, transport)

    // 5. Register the agent and declare which remote extensions it will use.
    Agent(
        protocol = protocol,
        agentSupport = TerminalAgentSupport(),
    )

    protocol.start()
}
