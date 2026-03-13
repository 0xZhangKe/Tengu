package com.tengu.app.framework.http

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object GlobalOkHttpClient {

    private const val TIMEOUT = 15L

    val client: OkHttpClient by lazy { createBuilder().build() }

    private val thirdPartInterceptors = mutableListOf<Interceptor>()

    fun addThirdPartInterceptor(interceptor: Interceptor) {
        thirdPartInterceptors += interceptor
    }

    private fun createBuilder(): OkHttpClient.Builder {
        val ssl = buildSSLFactory()
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .callTimeout(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .sslSocketFactory(ssl.first, ssl.second)
            .hostnameVerifier { _, _ -> true }
        if (SharedHttpClientConfig.enableLogging) {
            builder.addInterceptor(
                HttpLoggingInterceptor(HttpLoggingInterceptor.Logger.DEFAULT)
                    .setLevel(HttpLoggingInterceptor.Level.HEADERS)
            )
        }
        thirdPartInterceptors.forEach {
            builder.addInterceptor(it)
        }
        return builder
    }

    private fun buildSSLFactory(): Pair<SSLSocketFactory, X509TrustManager> {
        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers = trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            "Unexpected default trust managers:${trustManagers.contentToString()}"
        }
        val trustManager = trustManagers[0] as X509TrustManager
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
        val sslSocketFactory = sslContext.socketFactory
        return Pair(sslSocketFactory, trustManager)
    }
}
