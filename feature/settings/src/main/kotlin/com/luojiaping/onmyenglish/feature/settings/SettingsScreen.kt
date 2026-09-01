package com.luojiaping.onmyenglish.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luojiaping.onmyenglish.core.designsystem.OmeSpacing
import com.luojiaping.onmyenglish.core.ui.AppPage
import com.luojiaping.onmyenglish.core.ui.SettingsGroupCard
import com.luojiaping.onmyenglish.core.ui.SettingsGroupRow

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = state.page != SettingsPage.HOME) {
        viewModel.openPage(SettingsPage.HOME)
    }

    when (state.page) {
        SettingsPage.HOME -> SettingsHome(
            state = state,
            onOpenAiProvider = { viewModel.openPage(SettingsPage.AI_PROVIDER) },
            modifier = modifier,
        )
        SettingsPage.AI_PROVIDER -> AiProviderPage(
            state = state,
            onBack = { viewModel.openPage(SettingsPage.HOME) },
            onBaseUrlChange = viewModel::updateBaseUrl,
            onApiKeyChange = viewModel::updateApiKey,
            onChatModelChange = viewModel::updateChatModel,
            onVisionModelChange = viewModel::updateVisionModel,
            onFetchModels = viewModel::fetchModels,
            onSave = viewModel::save,
            modifier = modifier,
        )
    }
}

@Composable
private fun SettingsHome(
    state: SettingsUiState,
    onOpenAiProvider: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val versionName = rememberVersionName(context)

    AppPage(title = "设置", modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OmeSpacing.page, vertical = OmeSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(OmeSpacing.small),
        ) {
            SettingsGroupCard {
                SettingsGroupRow(
                    title = "AI 供应商",
                    icon = Icons.Outlined.Settings,
                    summary = if (state.isConfigured) state.visionModel else "未配置",
                    onClick = onOpenAiProvider,
                )
                SettingsGroupRow(
                    title = "学习偏好",
                    icon = Icons.Outlined.Build,
                    summary = "未开放",
                    enabled = false,
                    onClick = {},
                )
                SettingsGroupRow(
                    title = "数据管理",
                    icon = Icons.Outlined.Delete,
                    summary = "未开放",
                    enabled = false,
                    onClick = {},
                )
                SettingsGroupRow(
                    title = "关于",
                    icon = Icons.Outlined.Info,
                    summary = versionName,
                    enabled = false,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun rememberVersionName(context: android.content.Context): String =
    androidx.compose.runtime.remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
}

@Composable
private fun AiProviderPageBackAction(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(
            Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "返回",
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AiProviderPage(
    state: SettingsUiState,
    onBack: () -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onChatModelChange: (String) -> Unit,
    onVisionModelChange: (String) -> Unit,
    onFetchModels: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppPage(
        title = "AI 供应商",
        modifier = modifier,
        actions = { AiProviderPageBackAction(onBack) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = OmeSpacing.page, vertical = OmeSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(OmeSpacing.medium),
        ) {
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = onBaseUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("接口地址") },
                singleLine = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key（本地模型可留空）") },
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = if (state.apiKey.isBlank()) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            if (state.baseUrl.startsWith("http://")) {
                Text(
                    text = "HTTP 仅允许无密钥的本地模型服务",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            ModelSelectorField(
                label = "对话模型",
                value = state.chatModel,
                models = state.models,
                modelsLoaded = state.modelsLoaded,
                isFetching = state.isFetchingModels,
                onValueChange = onChatModelChange,
                onFetch = onFetchModels,
            )
            ModelSelectorField(
                label = "视觉模型",
                value = state.visionModel,
                models = state.models,
                modelsLoaded = state.modelsLoaded,
                isFetching = state.isFetchingModels,
                onValueChange = onVisionModelChange,
                onFetch = onFetchModels,
            )

            state.statusMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && !state.isLoading,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (state.isSaving) "正在保存" else "保存")
            }
        }
    }
}
