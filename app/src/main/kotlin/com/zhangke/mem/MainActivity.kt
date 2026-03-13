package com.tengu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tengu.app.framework.http.SharedHttpClientConfig
import com.tengu.app.hosting.TenguApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (BuildConfig.DEBUG) {
            SharedHttpClientConfig.enableLogging = true
        }
        setContent {
            TenguApp()
        }
    }
}
