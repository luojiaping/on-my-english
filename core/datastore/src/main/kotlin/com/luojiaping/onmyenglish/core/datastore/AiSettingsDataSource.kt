package com.luojiaping.onmyenglish.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.luojiaping.onmyenglish.core.model.AiProviderSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.aiSettingsDataStore by preferencesDataStore(name = "ai_settings")

@Singleton
class AiSettingsDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val secureKeyStore: SecureKeyStore,
) {
    val settings: Flow<AiProviderSettings> = context.aiSettingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(androidx.datastore.preferences.core.emptyPreferences())
            else throw error
        }
        .map { preferences ->
            AiProviderSettings(
                baseUrl = preferences[Keys.BASE_URL] ?: AiProviderSettings.DEFAULT_BASE_URL,
                apiKey = preferences[Keys.ENCRYPTED_API_KEY]
                    ?.let(secureKeyStore::decryptOrNull)
                    .orEmpty(),
                chatModel = preferences[Keys.CHAT_MODEL] ?: "gpt-4.1-mini",
                visionModel = preferences[Keys.VISION_MODEL] ?: "gpt-4.1-mini",
                temperature = preferences[Keys.TEMPERATURE] ?: 0.2,
            )
        }

    suspend fun save(settings: AiProviderSettings) {
        val encryptedKey = secureKeyStore.encrypt(settings.apiKey)
        context.aiSettingsDataStore.edit { preferences ->
            preferences[Keys.BASE_URL] = settings.baseUrl
            preferences[Keys.ENCRYPTED_API_KEY] = encryptedKey
            preferences[Keys.CHAT_MODEL] = settings.chatModel
            preferences[Keys.VISION_MODEL] = settings.visionModel
            preferences[Keys.TEMPERATURE] = settings.temperature
        }
    }

    private object Keys {
        val BASE_URL = stringPreferencesKey("base_url")
        val ENCRYPTED_API_KEY = stringPreferencesKey("encrypted_api_key")
        val CHAT_MODEL = stringPreferencesKey("chat_model")
        val VISION_MODEL = stringPreferencesKey("vision_model")
        val TEMPERATURE = doublePreferencesKey("temperature")
    }
}
