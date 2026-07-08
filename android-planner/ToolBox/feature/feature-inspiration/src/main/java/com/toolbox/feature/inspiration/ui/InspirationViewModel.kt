package com.toolbox.feature.inspiration.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toolbox.core.websnapshot.WebSnapshotEngine
import com.toolbox.feature.inspiration.data.InspirationNote
import com.toolbox.feature.inspiration.data.InspirationRepository
import com.toolbox.feature.inspiration.data.InspirationTemplate
import com.toolbox.feature.inspiration.processor.AiNoteProcessor
import com.toolbox.feature.inspiration.processor.AiProcessResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class InspirationViewModel @Inject constructor(
    private val repository: InspirationRepository,
    private val aiProcessor: AiNoteProcessor,
    private val webSnapshotEngine: WebSnapshotEngine
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _event = MutableStateFlow<InspirationEvent?>(null)
    val event: StateFlow<InspirationEvent?> = _event.asStateFlow()

    val notes: StateFlow<List<InspirationNote>> = combine(
        repository.observeAll(),
        _searchQuery,
        _selectedCategory
    ) { notes, query, category ->
        notes.filter { note ->
            (category == null || note.category == category) &&
                (query.isBlank() || note.title.contains(query, ignoreCase = true) ||
                    note.content?.contains(query, ignoreCase = true) == true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<InspirationTemplate>> = repository.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initBuiltinTemplates()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String?) {
        _selectedCategory.value = category
    }

    fun createNote(
        title: String,
        content: String?,
        templateId: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.create(title, content, templateId)
                _event.value = InspirationEvent.Created
            } catch (e: Exception) {
                _event.value = InspirationEvent.Error(e.message ?: "创建失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNote(
        id: String,
        title: String,
        content: String?,
        templateId: String?,
        priority: Int
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.update(id, title, content, templateId, priority)
                _event.value = InspirationEvent.Updated
            } catch (e: Exception) {
                _event.value = InspirationEvent.Error(e.message ?: "更新失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            try {
                repository.delete(id)
                _event.value = InspirationEvent.Deleted
            } catch (e: Exception) {
                _event.value = InspirationEvent.Error(e.message ?: "删除失败")
            }
        }
    }

    fun processWithAi(noteId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = aiProcessor.processNote(noteId)
                when (result) {
                    is AiProcessResult.Success -> {
                        _event.value = InspirationEvent.AiProcessed(
                            "分类: ${result.category ?: "未知"}，标签: ${result.tags.joinToString()}"
                        )
                    }
                    is AiProcessResult.PartialSuccess -> {
                        _event.value = InspirationEvent.AiProcessed(
                            "部分成功: ${result.succeeded} 成功，${result.failed} 失败"
                        )
                    }
                    is AiProcessResult.Error -> {
                        _event.value = InspirationEvent.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _event.value = InspirationEvent.Error(e.message ?: "AI 处理失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processAllUnprocessed() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = aiProcessor.processAllUnprocessed()
                val successCount = results.count { it is AiProcessResult.Success }
                _event.value = InspirationEvent.AiProcessed(
                    "处理完成: $successCount/${results.size} 成功"
                )
            } catch (e: Exception) {
                _event.value = InspirationEvent.Error(e.message ?: "批量处理失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun captureWebPage(url: String, outputDir: File) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val filePath = webSnapshotEngine.capture(url, outputDir)
                _event.value = InspirationEvent.WebCaptured(filePath)
            } catch (e: Exception) {
                _event.value = InspirationEvent.Error(e.message ?: "网页快照失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAttachment(noteId: String, type: String, filePath: String?, url: String?) {
        viewModelScope.launch {
            try {
                repository.addAttachment(noteId, type, filePath, url)
            } catch (e: Exception) {
                _event.value = InspirationEvent.Error(e.message ?: "添加附件失败")
            }
        }
    }

    fun clearEvent() {
        _event.value = null
    }
}

sealed class InspirationEvent {
    data object Created : InspirationEvent()
    data object Updated : InspirationEvent()
    data object Deleted : InspirationEvent()
    data class AiProcessed(val summary: String) : InspirationEvent()
    data class WebCaptured(val filePath: String) : InspirationEvent()
    data class Error(val message: String) : InspirationEvent()
}
