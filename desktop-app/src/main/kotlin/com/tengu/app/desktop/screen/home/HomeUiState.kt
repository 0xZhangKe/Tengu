package com.tengu.app.desktop.screen.home

import com.tengu.app.common.ui.model.ChatMessage
import com.tengu.app.common.ui.model.ChatMessageRole
import kotlin.time.Clock

data class HomeUiState(
    val connected: Boolean,
    val status: String,
    val path: String?,
    val messages: List<ChatMessage>,
) {

    companion object {

        fun default(): HomeUiState {
            return HomeUiState(
                connected = false,
                status = "",
                path = null,
                messages = emptyList(),
            )
        }

        private fun mockMessageList(): List<ChatMessage>{
            return listOf(
                ChatMessage.Text(
                    text = "Hi",
                    role = ChatMessageRole.USER,
                    createAt = Clock.System.now(),
                ),
                ChatMessage.Text(
                    text = "Hi, I`m codex cli, what can I help you?",
                    role = ChatMessageRole.ASSISTANT,
                    createAt = Clock.System.now(),
                ),
                ChatMessage.Text(
                    text = "帮我总结这个项目的作用",
                    role = ChatMessageRole.USER,
                    createAt = Clock.System.now(),
                ),
                ChatMessage.Text(
                    text = "短结论：当前不能通过 ACP 协议拿到 Codex App 或 Codex CLI 已产生的 thread",
                    role = ChatMessageRole.ASSISTANT,
                    createAt = Clock.System.now(),
                ),
                ChatMessage.Text(
                    text = "继续",
                    role = ChatMessageRole.USER,
                    createAt = Clock.System.now(),
                ),
                ChatMessage.Text(
                    text = """
                        补充一点：Codex 本地确实有 thread/session 存储，比如 session_index.jsonl (line 1) 里能看到 id/thread_name/updated_at。但这说明的是“本地有存储”，不等于 ACP 对外暴露了读取这些 thread 的协议接口。目前看，codex resume / fork 走的是 Codex 自己的本地会话机制，不是 ACP 给第三方客户端开放的能力。

                        所以结论更准确地说是：

                        通过当前 codex-acp 的受支持 ACP 能力：不行
                        直接读 ~/.codex/... 内部文件：可以研究，但这是非官方/非稳定接口，格式可能变
                        如果你要，我可以下一步直接帮你设计一个“旁路读取 ~/.codex/session_index.jsonl + session 文件”的适配层，把历史 thread 映射成你项目里的会话列表。
                    """.trimIndent(),
                    role = ChatMessageRole.ASSISTANT,
                    createAt = Clock.System.now(),
                )
            )
        }
    }
}
