package com.tengu.app.common.ui

import kotlin.time.Instant

sealed interface MessageType {

    data class Text(val text: String): MessageType
}
