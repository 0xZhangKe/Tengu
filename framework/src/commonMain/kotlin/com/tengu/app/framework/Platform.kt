package com.tengu.app.framework

object Platform {
    val name: String = getPlatformName()
}

expect fun getPlatformName(): String
