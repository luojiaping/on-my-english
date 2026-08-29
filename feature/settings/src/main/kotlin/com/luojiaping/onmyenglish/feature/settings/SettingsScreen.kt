package com.luojiaping.onmyenglish.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NetworkCheck
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luojiaping.onmyenglish.core.designsystem.OmeSpacing
import com.luojiaping.onmyenglish.core.ui.AppPage
import java.util.Locale

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsScreen(
        state = state,
        onBaseUrlChange = viewModel::updateBaseUrl,
        onApiKeyChange = viewModel::updateApiKey,
        onChatModelChange = viewModel::updateChatModel,
        onVisionModelChange = viewModel::updateVisionModel,
        onTemperatureChange = viewModel::updateTemperature,
        onSave = viewModel::save,
        onTestConnection = viewModel::testConnection,
        modifier = modifier,
    )
}

@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    onBaseUrlChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onChatModelChange: (String) -> Unit,
    onVisionModelChange: (String) -> Unit,
    onTemperatureChange: (Float) -> Unit,
    onSave: () -> Unit,
    onTestConnection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showApiKey by remember { mutableStateOf(false) }

    AppPage(title = "设置", modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(OmeSpacing.page),
            verticalArrangement = Arrangement.spacedBy(OmeSpacing.medium),
        ) {
            Text("AI 供应商", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.baseUrl,
                onValueChange = onBaseUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("OpenAI 兼容接口地址") },
                singleLine = true,
                enabled = !state.isLoading,
            )
            OutlinedTextField(
                value = state.apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key（本地模型可留空）") },
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = if (showApiKey) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showApiKey, onCheckedChange = { showApiKey = it })
                Text("显示 API Key", style = MaterialTheme.typography.bodyMedium)
            }
            if (state.baseUrl.startsWith("http://")) {
                Text(
                    text = "HTTP 仅允许无密钥的本地模型服务",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("模型", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state.chatModel,
                onValueChange = onChatModelChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("对话模型") },
                singleLine = true,
            )
            OutlinedTextField(
                value = state.visionModel,
                onValueChange = onVisionModelChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("视觉模型") },
                singleLine = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("温度", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = String.format(Locale.US, "%.1f", state.temperature),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = state.temperature,
                onValueChange = onTemperatureChange,
                valueRange = 0f..2f,
                steps = 19,
            )

            state.statusMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OmeSpacing.small),
            ) {
                OutlinedButton(
                    onClick = onTestConnection,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isTesting && !state.isLoading,
                ) {
                    if (state.isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.NetworkCheck, contentDescription = null)
                    }
                    Text("测试连接", modifier = Modifier.padding(start = OmeSpacing.small))
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isSaving && !state.isLoading,
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(Icons.Outlined.Save, contentDescription = null)
                    }
                    Text("保存", modifier = Modifier.padding(start = OmeSpacing.small))
                }
            }
        }
    }
}
