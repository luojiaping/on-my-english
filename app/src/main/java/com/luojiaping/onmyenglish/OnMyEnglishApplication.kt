package com.luojiaping.onmyenglish

import android.app.Application
import com.luojiaping.onmyenglish.core.data.BuiltInDeckSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class OnMyEnglishApplication : Application() {
    @Inject
    lateinit var builtInDeckSeeder: BuiltInDeckSeeder

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch { builtInDeckSeeder.seedIfNeeded() }
    }
}
