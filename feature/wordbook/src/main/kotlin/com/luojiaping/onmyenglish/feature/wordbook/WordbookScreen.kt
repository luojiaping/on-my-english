package com.luojiaping.onmyenglish.feature.wordbook

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.luojiaping.onmyenglish.core.designsystem.OmeSpacing
import com.luojiaping.onmyenglish.core.model.ExtractedWord
import com.luojiaping.onmyenglish.core.model.Word
import com.luojiaping.onmyenglish.core.ui.AppPage
import com.luojiaping.onmyenglish.core.ui.EmptyState
import java.io.File

@Composable
fun WordbookRoute(
    modifier: Modifier = Modifier,
    viewModel: WordbookViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let { viewModel.selectImage(it.toString()) } }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { saved ->
        if (saved) pendingCameraUri?.let(viewModel::selectImage)
        pendingCameraUri = null
    }

    WordbookScreen(
        state = state,
        onPickImage = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
        onTakePhoto = {
            runCatching {
                createCameraUri(context).also { uri ->
                    pendingCameraUri = uri.toString()
                    cameraLauncher.launch(uri)
                }
            }.onFailure {
                pendingCameraUri = null
                viewModel.reportError(it.message ?: "无法启动相机")
            }
        },
        onToggleCandidate = viewModel::toggleCandidate,
        onUpdateCandidate = viewModel::updateCandidate,
        onDeckNameChange = viewModel::updateDeckName,
        onImport = viewModel::importSelected,
        onDismissImport = viewModel::dismissImport,
        modifier = modifier,
    )
}

@Composable
private fun WordbookScreen(
    state: WordbookUiState,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onToggleCandidate: (Int) -> Unit,
    onUpdateCandidate: (Int, ExtractedWord) -> Unit,
    onDeckNameChange: (String) -> Unit,
    onImport: () -> Unit,
    onDismissImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppPage(title = "词库", modifier = modifier) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OmeSpacing.page, vertical = OmeSpacing.medium),
                horizontalArrangement = Arrangement.spacedBy(OmeSpacing.small),
            ) {
                FilledTonalButton(onClick = onTakePhoto, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null)
                    Text("拍照导入", modifier = Modifier.padding(start = OmeSpacing.small))
                }
                OutlinedButton(onClick = onPickImage, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
                    Text("相册导入", modifier = Modifier.padding(start = OmeSpacing.small))
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OmeSpacing.page, vertical = OmeSpacing.small),
                horizontalArrangement = Arrangement.spacedBy(OmeSpacing.large),
            ) {
                CountMetric(label = "词条", count = state.words.size)
                CountMetric(label = "词书", count = state.decks.size)
            }
            state.statusMessage?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = OmeSpacing.page, vertical = OmeSpacing.small),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (state.words.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(title = "词库为空", icon = Icons.Outlined.MenuBook)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.words, key = Word::id) { word ->
                        WordRow(word)
                    }
                }
            }
        }
    }

    if (state.showImportSheet) {
        ImportSheet(
            state = state,
            onToggleCandidate = onToggleCandidate,
            onUpdateCandidate = onUpdateCandidate,
            onDeckNameChange = onDeckNameChange,
            onImport = onImport,
            onDismiss = onDismissImport,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ImportSheet(
    state: WordbookUiState,
    onToggleCandidate: (Int) -> Unit,
    onUpdateCandidate: (Int, ExtractedWord) -> Unit,
    onDeckNameChange: (String) -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editingIndex by remember { mutableStateOf<Int?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OmeSpacing.page)
                .padding(bottom = OmeSpacing.large),
            verticalArrangement = Arrangement.spacedBy(OmeSpacing.medium),
        ) {
            Text("识图导入", style = MaterialTheme.typography.headlineSmall)
            state.selectedImageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "待识别图片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            when {
                state.isExtracting -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = OmeSpacing.large),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text("正在识别", modifier = Modifier.padding(start = OmeSpacing.medium))
                }
                state.candidates.isNotEmpty() -> {
                    Text(
                        text = "候选词条 ${state.selectedCandidates.size}/${state.candidates.size}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        itemsIndexed(state.candidates) { index, candidate ->
                            CandidateRow(
                                candidate = candidate,
                                selected = index in state.selectedCandidates,
                                onToggle = { onToggleCandidate(index) },
                                onEdit = { editingIndex = index },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.deckName,
                        onValueChange = onDeckNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("目标词书") },
                        singleLine = true,
                    )
                }
            }
            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            if (state.candidates.isNotEmpty()) {
                Button(
                    onClick = onImport,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isImporting && state.selectedCandidates.isNotEmpty(),
                ) {
                    if (state.isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Text(
                        text = if (state.isImporting) "正在写入" else "导入词库",
                        modifier = Modifier.padding(start = if (state.isImporting) OmeSpacing.small else 0.dp),
                    )
                }
            }
        }
    }

    editingIndex?.let { index ->
        state.candidates.getOrNull(index)?.let { candidate ->
            CandidateEditor(
                candidate = candidate,
                onSave = {
                    onUpdateCandidate(index, it)
                    editingIndex = null
                },
                onDismiss = { editingIndex = null },
            )
        }
    }
}

@Composable
private fun CandidateRow(
    candidate: ExtractedWord,
    selected: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = OmeSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = selected, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f)) {
            Text(candidate.headword, style = MaterialTheme.typography.titleMedium)
            Text(
                text = candidate.translation.ifBlank { candidate.definition },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Outlined.Edit, contentDescription = "编辑 ${candidate.headword}")
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun CandidateEditor(
    candidate: ExtractedWord,
    onSave: (ExtractedWord) -> Unit,
    onDismiss: () -> Unit,
) {
    var headword by remember(candidate) { mutableStateOf(candidate.headword) }
    var phonetic by remember(candidate) { mutableStateOf(candidate.phonetic) }
    var definition by remember(candidate) { mutableStateOf(candidate.definition) }
    var translation by remember(candidate) { mutableStateOf(candidate.translation) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑词条") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(OmeSpacing.small)) {
                OutlinedTextField(headword, { headword = it }, label = { Text("单词") })
                OutlinedTextField(phonetic, { phonetic = it }, label = { Text("音标") })
                OutlinedTextField(definition, { definition = it }, label = { Text("英文释义") })
                OutlinedTextField(translation, { translation = it }, label = { Text("中文释义") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        candidate.copy(
                            headword = headword.trim(),
                            phonetic = phonetic.trim(),
                            definition = definition.trim(),
                            translation = translation.trim(),
                        ),
                    )
                },
                enabled = headword.isNotBlank() && definition.isNotBlank(),
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun WordRow(word: Word) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OmeSpacing.page, vertical = OmeSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(word.headword, style = MaterialTheme.typography.titleMedium)
            if (word.phonetic.isNotBlank()) {
                Text(
                    text = word.phonetic,
                    modifier = Modifier.padding(start = OmeSpacing.small),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        val sense = word.senses.firstOrNull()
        sense?.let {
            Text(
                text = it.translation.ifBlank { it.definition },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun CountMetric(label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = OmeSpacing.small),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun createCameraUri(context: Context): Uri {
    val directory = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File.createTempFile("vocabulary-", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
