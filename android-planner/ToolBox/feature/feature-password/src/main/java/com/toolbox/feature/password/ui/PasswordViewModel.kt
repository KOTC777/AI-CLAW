package com.toolbox.feature.password.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toolbox.core.security.crypto.SecurityManager
import com.toolbox.feature.password.data.PasswordEntry
import com.toolbox.feature.password.data.PasswordGroup
import com.toolbox.feature.password.data.PasswordRepository
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
class PasswordViewModel @Inject constructor(
    private val repository: PasswordRepository,
    private val securityManager: SecurityManager
) : ViewModel() {

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGroupId = MutableStateFlow<String?>(null)
    val selectedGroupId: StateFlow<String?> = _selectedGroupId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _event = MutableStateFlow<PasswordEvent?>(null)
    val event: StateFlow<PasswordEvent?> = _event.asStateFlow()

    private val _selectedEntry = MutableStateFlow<PasswordEntry?>(null)
    val selectedEntry: StateFlow<PasswordEntry?> = _selectedEntry.asStateFlow()

    val groups: StateFlow<List<PasswordGroup>> = repository.observeGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val entries: StateFlow<List<PasswordEntry>> = combine(
        repository.observeAllEntries(),
        _searchQuery,
        _selectedGroupId
    ) { entries, query, groupId ->
        entries
            .filter { entry ->
                (groupId == null || entry.groupId == groupId) &&
                    (query.isBlank() || entry.title.contains(query, ignoreCase = true))
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unlock(password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                securityManager.unlock(password.toCharArray())
                _isUnlocked.value = true
                _event.value = PasswordEvent.Unlocked
            } catch (e: Exception) {
                _event.value = PasswordEvent.Error("密码错误或解锁失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun lock() {
        securityManager.lock()
        _isUnlocked.value = false
        _selectedEntry.value = null
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onGroupSelected(groupId: String?) {
        _selectedGroupId.value = groupId
    }

    fun loadEntryDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val entry = repository.getEntryWithPassword(id)
                _selectedEntry.value = entry
            } catch (e: Exception) {
                _event.value = PasswordEvent.Error("加载失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createEntry(title: String, groupId: String?, icon: String?, password: String, hints: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.createEntry(title, groupId, icon, password, hints)
                _event.value = PasswordEvent.EntryCreated
            } catch (e: Exception) {
                _event.value = PasswordEvent.Error("创建失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEntry(id: String, title: String, groupId: String?, icon: String?, password: String?, hints: List<String>?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateEntry(id, title, groupId, icon, password, hints)
                _event.value = PasswordEvent.EntryUpdated
            } catch (e: Exception) {
                _event.value = PasswordEvent.Error("更新失败: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteEntry(id)
                _event.value = PasswordEvent.EntryDeleted
            } catch (e: Exception) {
                _event.value = PasswordEvent.Error("删除失败: ${e.message}")
            }
        }
    }

    fun createGroup(name: String, icon: String?, color: Int?) {
        viewModelScope.launch {
            try {
                repository.createGroup(name, icon, color)
            } catch (e: Exception) {
                _event.value = PasswordEvent.Error("创建分组失败: ${e.message}")
            }
        }
    }

    fun clearEvent() {
        _event.value = null
    }
}

sealed class PasswordEvent {
    data object Unlocked : PasswordEvent()
    data object EntryCreated : PasswordEvent()
    data object EntryUpdated : PasswordEvent()
    data object EntryDeleted : PasswordEvent()
    data class Error(val message: String) : PasswordEvent()
}
