package com.luojiaping.onmyenglish.core.common

import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher

enum class AppDispatcher {
    DEFAULT,
    IO,
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val value: AppDispatcher)

interface DispatcherProvider {
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
}
