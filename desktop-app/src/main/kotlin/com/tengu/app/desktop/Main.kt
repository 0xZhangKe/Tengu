package com.tengu.app.desktop

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.tengu.app.desktop.composable.LocalWindowScope
import com.tengu.app.desktop.screen.TenguApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin {
        modules(mainModule)
    }
    if (args.contains("--cli-client")) {
        val prompt = args.dropWhile { it != "--cli-client" }.drop(1).joinToString(" ").ifBlank {
            "Hello from ACP client. Please reply with one short sentence."
        }
        runBlocking {
            runCliClient(this, prompt, "./")
        }
    } else {
        startDesktopApp()
    }
}

private fun startDesktopApp() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "",
            undecorated = false,
        ) {
            LaunchedEffect(Unit) {
                window.rootPane.putClientProperty("apple.awt.fullWindowContent", true)
                window.rootPane.putClientProperty("apple.awt.transparentTitleBar", true)
                window.rootPane.putClientProperty("apple.awt.windowTitleVisible", false)
            }
            CompositionLocalProvider(
                LocalWindowScope provides this
            ) {
                TenguApp()
            }
        }
    }
}

private suspend fun runCliClient(
    coroutineScope: CoroutineScope,
    prompt: String,
    projectDir: String,
) {
    val session = createCodexChatSession(coroutineScope, projectDir)
    try {
        val result = session.send(prompt)
        println(result.reply)
        println("[done] ${result.stopReason}")
    } finally {
        session.close()
    }
}
