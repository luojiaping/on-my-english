package com.luojiaping.onmyenglish.feature.wordbook

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.luojiaping.onmyenglish.core.designsystem.DeckAccent
import com.luojiaping.onmyenglish.core.designsystem.DeckCapsuleCard
import com.luojiaping.onmyenglish.core.designsystem.OmeSpacing
import com.luojiaping.onmyenglish.core.model.Deck
import com.luojiaping.onmyenglish.core.model.DeckCategory
import com.luojiaping.onmyenglish.core.model.ExtractedWord
import com.luojiaping.onmyenglish.core.ui.AppPage
import java.io.File

@Composable
fun WordbookRoute(
    modifier: Modifier = Modifier,
    viewModel: WordbookViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val detail by viewModel.detailState.collectAsStateWithLifecycle()
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

    val launchCamera = {
        runCatching {
            createCameraUri(context).also { uri ->
                pendingCameraUri = uri.toString()
                cameraLauncher.launch(uri)
            }
        }.onFailure {
            pendingCameraUri = null
            viewModel.reportError(it.message ?: "无法启动相机")
        }
    }
    val launchGallery = {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    if (detail.deck != null) {
        DeckDetailScreen(
            state = detail,
            onBack = { viewModel.openDeck(null) },
            onSearch = viewModel::updateSearch,
        )
        return
    }

    WordbookScreen(
        state = state,
        onOpenDeck = { viewModel.openDeck(it) },
        onTakePhoto = launchCamera,
        onPickImage = launchGallery,
        onToggleCandidate = viewModel::toggleCandidate,
        onUpdateCandidate = viewModel::updateCandidate,
        onDeckNameChange = viewModel::updateDeckName,
        onImport = viewModel.importSelected,
        onDismissImport = viewModel.dismissImport,
        modifier = modifier,
    )
}

@Composable
private fun WordbookScreen(
    state: WordbookUiState,
    onOpenDeck: (String) -> Unit,
    onPickImage: () -> Unit,
    onTakePhoto: () -> Unit,
    onToggleCandidate: (Int) -> Unit,
    onUpdateCandidate: (Int, ExtractedWord) -> Unit,
    onDeckNameChange: (String) -> Unit,
    onImport: () -> Unit,
    onDismissImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppPage(
        title = "词库",
        modifier = modifier,
        actions = {
            IconButton(onClick = onTakePhoto) {
                Icon(
                    Icons.Outlined.Create,
                    contentDescription = "拍照识词",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = OmeSpacing.page,
                vertical = OmeSpacing.medium,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.builtInDecks.isNotEmpty()) {
                item(key = "header-builtin") {
                    SectionHeader(title = "内置词库", chip = "ECDICT")
                }
                items(state.builtInDecks, key = Deck::id) { deck ->
                    DeckCapsuleCard(
                        title = deck.name,
                        subtitle = "已掌握 ${deck.learnedCount}",
                        badge = deck.badge.ifBlank { deck.name.take(1) },
                        accent = accentFor(deck),
                        wordCount = deck.wordCount,
                        learnedCount = deck.learnedCount,
                        onClick = { onOpenDeck(deck.id) },
                        coverUri = deck.coverUri,
                        trailingChip = "内置",
                    )
                }
            }

            item(key = "header-custom") {
                SectionHeader(title = "我的词库", chip = "AI 识图")
            }
            if (state.customDecks.isEmpty()) {
                item(key = "empty-hint") {
                    Text(
                        text = "还没有自定义词库，拍一张单词书的照片试试",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
            items(state.customDecks, key = Deck::id) { deck ->
                DeckCapsuleCard(
                    title = deck.name,
                    subtitle = "已掌握 ${deck.learnedCount}",
                    badge = deck.badge.ifBlank { "词" },
                    accent = accentFor(deck),
                    wordCount = deck.wordCount,
                    learnedCount = deck.learnedCount,
                    onClick = { onOpenDeck(deck.id) },
                    coverUri = deck.coverUri,
                    coverImage = deck.coverUri?.let { uri ->
                        {
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(50)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    },
                    trailingChip = "AI 识图",
                )
            }

            item(key = "cta") {
                DashedNewDeckCard(
                    onTakePhoto = onTakePhoto,
                    onPickImage = onPickImage,
                )
            }

            state.statusMessage?.let { message ->
                item(key = "status") {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
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
private fun SectionHeader(title: String, chip: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = chip,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun accentFor(deck: Deck): DeckAccent = when (deck.badge) {
    "四" -> DeckAccent.PRIMARY
    "六" -> DeckAccent.SECONDARY
    "研" -> DeckAccent.TERTIARY
    else -> DeckAccent.NEUTRAL
}

@Composable
private fun DashedNewDeckCard(
    onTakePhoto: () -> Unit,
    onPickImage: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
        border = BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(OmeSpacing.small),
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "拍照 / 相册 识图导入",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onTakePhoto) { Text("拍照") }
            TextButton(onClick = onPickImage) { Text("相册") }
        }
    }
}

@Composable
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
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
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
                        modifier = Modifier.padding(
                            start = if (state.isImporting) OmeSpacing.small else 0.dp,
                        ),
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

private fun createCameraUri(context: Context): Uri {
    val directory = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File.createTempFile("vocabulary-", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
