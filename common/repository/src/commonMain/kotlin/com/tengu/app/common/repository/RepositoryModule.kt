package com.tengu.app.common.repository

import com.tengu.app.framework.http.sharedHttpClient
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

val repositoryModule = module {

    includes(createPlatformModule())

    single<HttpClient> { sharedHttpClient }
}

expect fun createPlatformModule(): Module
