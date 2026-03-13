package com.tengu.app

import android.app.Application
import com.tengu.app.framework.util.initApplication
import com.tengu.app.hosting.TenguApplication

class TenguAndroidApplication : Application() {

    override fun onCreate() {
        initApplication(this)
        super.onCreate()
        TenguApplication.initialize()
    }
}
