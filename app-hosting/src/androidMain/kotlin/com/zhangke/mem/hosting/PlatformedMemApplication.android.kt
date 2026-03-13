package com.tengu.app.hosting

import com.tengu.app.framework.util.appContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication

actual class PlatformedTenguApplication {

    actual fun KoinApplication.initKoin() {
        androidLogger()
        androidContext(appContext)
    }
}
