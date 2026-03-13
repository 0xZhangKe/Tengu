package com.tengu.app.framework.module

interface ModuleStartup {

    suspend fun onAppCreate()
}
