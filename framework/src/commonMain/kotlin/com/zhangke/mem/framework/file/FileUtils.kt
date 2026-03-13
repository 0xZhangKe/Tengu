package com.tengu.app.framework.file

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.readString

val cacheDirectoryPath: String = SystemTemporaryDirectory.toString()

fun readFileAsString(path: String): String {
    return SystemFileSystem.source(Path(path)).buffered().use {
        it.readString()
    }
}
