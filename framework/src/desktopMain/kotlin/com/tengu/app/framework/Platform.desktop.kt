package com.tengu.app.framework

actual fun getPlatformName(): String = "Desktop (${System.getProperty("os.name")})"
