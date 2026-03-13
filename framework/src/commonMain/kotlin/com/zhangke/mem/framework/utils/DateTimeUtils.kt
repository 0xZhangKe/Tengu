package com.tengu.app.framework.utils

import kotlinx.datetime.LocalDateTime

fun LocalDateTime.isDaytime(): Boolean {
    val hour = this.hour
    return hour in 6 until 18
}
