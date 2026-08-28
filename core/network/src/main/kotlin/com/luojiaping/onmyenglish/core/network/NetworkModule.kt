package com.luojiaping.onmyenglish.core.network

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NetworkJson

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @NetworkJson
    fun provideNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
        encodeDefaults = false
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        @NetworkJson json: Json,
    ): HttpClient = HttpClient(OkHttp) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 20_000
            requestTimeoutMillis = 90_000
            socketTimeoutMillis = 90_000
        }
        install(HttpRequestRetry) {
            maxRetries = 2
            retryIf { _, response ->
                response.status.value == 429 || response.status.value >= 500
            }
            retryOnException(maxRetries = 2, retryOnTimeout = true)
            exponentialDelay()
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
}
