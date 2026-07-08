package com.toolbox.feature.memo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.toolbox.feature.memo.data.Memo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoEditScreen(
    memoId: String?,
    onNavigateBack: () -> Unit,
    viewModel: MemoViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val event by viewModel.event.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isEditing by remember { mutableStateOf(memoId == null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var loadedMemo by remember { mutableStateOf<Memo?>(null) }
    val focusRequester = remember { FocusRequester() }

    // Load existing memo
    LaunchedEffect(memoId) {
        if (memoId != null) {
            // TODO: Load memo from repository via ViewModel
            // For now, use a separate flow
        }
    }

    // Handle events
    LaunchedEffect(event) {
        when (event) {
            is MemoEvent.Created, is MemoEvent.Updated, is MemoEvent.Deleted -> {
                onNavigateBack()
                viewModel.clearEvent()
            }
            else -> {}
        }
    }

    // Auto-focus title field for new memo
    LaunchedEffect(Unit) {
        if (memoId == null) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (memoId == null) "新建备忘录" else "编辑备忘录")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (memoId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            if (title.isNotBlank() || content.isNotBlank()) {
                                if (memoId == null) {
                                    viewModel.onCreateMemo(title, content)
                                } else {
                                    viewModel.onUpdateMemo(memoId, title, content)
                                }
                            }
                        },
                        enabled = (title.isNotBlank() || content.isNotBlank()) && !isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("标题") },
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("开始记录...") },
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除备忘录") },
            text = { Text("确定要删除这条备忘录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        memoId?.let { viewModel.onDeleteMemo(it) }
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
