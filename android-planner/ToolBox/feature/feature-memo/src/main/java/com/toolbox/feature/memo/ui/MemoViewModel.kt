package com.toolbox.feature.memo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toolbox.feature.memo.data.Memo
import com.toolbox.feature.memo.data.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoViewModel @Inject constructor(
    private val repository: MemoRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _event = MutableStateFlow<MemoEvent?>(null)
    val event: StateFlow<MemoEvent?> = _event.asStateFlow()

    val memos: StateFlow<List<Memo>> = combine(
        repository.observeAll(),
        _searchQuery
    ) { memos, query ->
        if (query.isBlank()) memos
        else memos.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCreateMemo(title: String, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.create(title, content)
                _event.value = MemoEvent.Created
            } catch (e: Exception) {
                _event.value = MemoEvent.Error(e.message ?: "创建失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onUpdateMemo(id: String, title: String, content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.update(id, title, content)
                _event.value = MemoEvent.Updated
            } catch (e: Exception) {
                _event.value = MemoEvent.Error(e.message ?: "更新失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onDeleteMemo(id: String) {
        viewModelScope.launch {
            try {
                repository.delete(id)
                _event.value = MemoEvent.Deleted
            } catch (e: Exception) {
                _event.value = MemoEvent.Error(e.message ?: "删除失败")
            }
        }
    }

    fun onTogglePin(id: String) {
        viewModelScope.launch {
            try {
                repository.togglePin(id)
            } catch (e: Exception) {
                _event.value = MemoEvent.Error(e.message ?: "操作失败")
            }
        }
    }

    fun clearEvent() {
        _event.value = null
    }
}

sealed class MemoEvent {
    data object Created : MemoEvent()
    data object Updated : MemoEvent()
    data object Deleted : MemoEvent()
    data class Error(val message: String) : MemoEvent()
}
