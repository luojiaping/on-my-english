package com.luojiaping.onmyenglish.core.data

import com.luojiaping.onmyenglish.core.common.AppDispatcher
import com.luojiaping.onmyenglish.core.common.Dispatcher
import com.luojiaping.onmyenglish.core.common.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @Dispatcher(AppDispatcher.DEFAULT)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Dispatcher(AppDispatcher.IO)
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideDispatcherProvider(
        @Dispatcher(AppDispatcher.DEFAULT) default: CoroutineDispatcher,
        @Dispatcher(AppDispatcher.IO) io: CoroutineDispatcher,
    ): DispatcherProvider = object : DispatcherProvider {
        override val default = default
        override val io = io
    }
}
