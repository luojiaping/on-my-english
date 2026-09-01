package com.luojiaping.onmyenglish.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.domain.AiSettingsRepository
import com.luojiaping.onmyenglish.core.domain.ListAiModelsUseCase
import com.luojiaping.onmyenglish.core.domain.SaveAiSettingsUseCase
import com.luojiaping.onmyenglish.core.model.AiProviderSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SettingsPage {
    HOME,
    AI_PROVIDER,
}

data class SettingsUiState(
    val page: SettingsPage = SettingsPage.HOME,
    val baseUrl: String = AiProviderSettings.DEFAULT_BASE_URL,
    val apiKey: String = "",
    val chatModel: String = "gpt-4.1-mini",
    val visionModel: String = "gpt-4.1-mini",
    val temperature: Float = 0.2f,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isFetchingModels: Boolean = false,
    val models: List<String> = emptyList(),
    val modelsLoaded: Boolean = false,
    val statusMessage: String? = null,
    val isError: Boolean = false,
) {
    val isConfigured: Boolean get() = apiKey.isNotBlank() || visionModel.isNotBlank()

    fun toSettings() = AiProviderSettings(
        baseUrl = baseUrl.trim(),
        apiKey = apiKey.trim(),
        chatModel = chatModel.trim(),
        visionModel = visionModel.trim(),
        temperature = temperature.toDouble(),
    )
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: AiSettingsRepository,
    private val saveSettings: SaveAiSettingsUseCase,
    private val listModels: ListAiModelsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settings.collectLatest { settings ->
                _uiState.update {
                    it.copy(
                        baseUrl = settings.baseUrl,
                        apiKey = settings.apiKey,
                        chatModel = settings.chatModel,
                        visionModel = settings.visionModel,
                        temperature = settings.temperature.toFloat(),
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun openPage(page: SettingsPage) {
        _uiState.update {
            it.copy(
                page = page,
                statusMessage = null,
                isError = false,
                models = emptyList(),
                modelsLoaded = false,
            )
        }
    }

    fun updateBaseUrl(value: String) = update { copy(baseUrl = value, statusMessage = null) }
    fun updateApiKey(value: String) = update { copy(apiKey = value, statusMessage = null) }
    fun updateChatModel(value: String) = update { copy(chatModel = value, statusMessage = null) }
    fun updateVisionModel(value: String) = update {
        copy(visionModel = value, modelsLoaded = false, statusMessage = null)
    }

    fun updateTemperature(value: Float) = update { copy(temperature = value) }

    fun save() {
        val settings = _uiState.value.toSettings()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, statusMessage = null) }
            when (val result = saveSettings(settings)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(isSaving = false, statusMessage = "已保存", isError = false)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(isSaving = false, statusMessage = result.error.message, isError = true)
                }
            }
        }
    }

    fun fetchModels() {
        val settings = _uiState.value.toSettings()
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingModels = true, statusMessage = null) }
            when (val result = listModels(settings)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isFetchingModels = false,
                        models = result.value,
                        modelsLoaded = true,
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(
                        isFetchingModels = false,
                        statusMessage = result.error.message,
                        isError = true,
                    )
                }
            }
        }
    }

    private fun update(transform: SettingsUiState.() -> SettingsUiState) {
        _uiState.update(transform)
    }
}
