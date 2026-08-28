package com.luojiaping.onmyenglish.feature.wordbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luojiaping.onmyenglish.core.common.AppResult
import com.luojiaping.onmyenglish.core.domain.ExtractWordsFromImageUseCase
import com.luojiaping.onmyenglish.core.domain.ImportWordsUseCase
import com.luojiaping.onmyenglish.core.domain.ObserveDecksUseCase
import com.luojiaping.onmyenglish.core.domain.ObserveWordsUseCase
import com.luojiaping.onmyenglish.core.model.Deck
import com.luojiaping.onmyenglish.core.model.ExtractedWord
import com.luojiaping.onmyenglish.core.model.Word
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordbookUiState(
    val words: List<Word> = emptyList(),
    val decks: List<Deck> = emptyList(),
    val selectedImageUri: String? = null,
    val candidates: List<ExtractedWord> = emptyList(),
    val selectedCandidates: Set<Int> = emptySet(),
    val deckName: String = "AI 识图词库",
    val showImportSheet: Boolean = false,
    val isExtracting: Boolean = false,
    val isImporting: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null,
)

@HiltViewModel
class WordbookViewModel @Inject constructor(
    observeWords: ObserveWordsUseCase,
    observeDecks: ObserveDecksUseCase,
    private val extractWords: ExtractWordsFromImageUseCase,
    private val importWords: ImportWordsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WordbookUiState())
    val uiState: StateFlow<WordbookUiState> = _uiState.asStateFlow()
    private var extractionJob: Job? = null

    init {
        viewModelScope.launch {
            combine(observeWords(), observeDecks()) { words, decks -> words to decks }
                .collect { (words, decks) ->
                    _uiState.update { it.copy(words = words, decks = decks) }
                }
        }
    }

    fun selectImage(uri: String) {
        extractionJob?.cancel()
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                candidates = emptyList(),
                selectedCandidates = emptySet(),
                showImportSheet = true,
                isExtracting = true,
                errorMessage = null,
                statusMessage = null,
            )
        }
        extractionJob = viewModelScope.launch {
            when (val result = extractWords(uri)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        candidates = result.value,
                        selectedCandidates = result.value.indices.toSet(),
                        isExtracting = false,
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(isExtracting = false, errorMessage = result.error.message)
                }
            }
        }
    }

    fun toggleCandidate(index: Int) {
        _uiState.update { state ->
            val selection = state.selectedCandidates.toMutableSet()
            if (!selection.add(index)) selection.remove(index)
            state.copy(selectedCandidates = selection)
        }
    }

    fun updateCandidate(index: Int, candidate: ExtractedWord) {
        _uiState.update { state ->
            if (index !in state.candidates.indices) return@update state
            state.copy(
                candidates = state.candidates.toMutableList().apply { this[index] = candidate },
            )
        }
    }

    fun updateDeckName(value: String) {
        _uiState.update { it.copy(deckName = value, errorMessage = null) }
    }

    fun importSelected() {
        val state = _uiState.value
        val selected = state.selectedCandidates.sorted().mapNotNull(state.candidates::getOrNull)
        if (selected.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "请至少选择一个词条") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, errorMessage = null) }
            when (val result = importWords(state.deckName, selected)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        showImportSheet = false,
                        isImporting = false,
                        selectedImageUri = null,
                        candidates = emptyList(),
                        selectedCandidates = emptySet(),
                        statusMessage = "已导入 ${result.value} 个词条",
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(isImporting = false, errorMessage = result.error.message)
                }
            }
        }
    }

    fun dismissImport() {
        if (_uiState.value.isImporting) return
        extractionJob?.cancel()
        _uiState.update {
            it.copy(
                showImportSheet = false,
                isExtracting = false,
                selectedImageUri = null,
                candidates = emptyList(),
                selectedCandidates = emptySet(),
                errorMessage = null,
            )
        }
    }

    fun reportError(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }
}
