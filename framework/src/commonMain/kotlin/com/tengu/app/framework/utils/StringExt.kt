package com.tengu.app.framework.utils

inline fun String?.ifNullOrEmpty(block: () -> String): String {
    if (this == null) return block()
    return ifEmpty(block)
}
