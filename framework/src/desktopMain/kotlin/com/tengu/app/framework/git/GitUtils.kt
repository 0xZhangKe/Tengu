package com.tengu.app.framework.git

import java.io.File

object GitUtils {

    fun getBranchName(dir: String): String? = runCatching {
        val process = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
            .directory(File(dir))
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().readText().trim()
    }.getOrNull()

}
