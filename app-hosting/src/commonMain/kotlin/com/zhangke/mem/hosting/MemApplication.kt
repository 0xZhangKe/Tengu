package com.tengu.app.hosting

import com.tengu.app.common.repository.repositoryModule
import com.tengu.app.feature.acp.acpKoinModule
import com.tengu.app.framework.module.ModuleStartup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

object TenguApplication {

    fun initialize() {
        val koin = startKoin {
            PlatformedTenguApplication().apply {
                initKoin()
            }
            modules(
                acpKoinModule,
                repositoryModule,
            )
        }
        CoroutineScope(Dispatchers.Main).launch {
            koin.koin.getAll<ModuleStartup>().forEach { it.onAppCreate() }
        }
    }
}

expect class PlatformedTenguApplication() {

    fun KoinApplication.initKoin()
}
