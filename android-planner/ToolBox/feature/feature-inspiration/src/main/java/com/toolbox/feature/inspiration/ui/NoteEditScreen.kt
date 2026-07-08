package com.toolbox.feature.inspiration.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.toolbox.feature.inspiration.data.InspirationNote
import com.toolbox.feature.inspiration.data.InspirationTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: String?,
    templateId: String?,
    onNavigateBack: () -> Unit,
    viewModel: InspirationViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val templates by viewModel.templates.collectAsState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf<InspirationTemplate?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var templateFields by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Load template
    LaunchedEffect(templateId) {
        if (templateId != null && noteId == null) {
            val template = templates.find { it.id == templateId }
            selectedTemplate = template
            template?.structure?.fields?.forEach { field ->
                templateFields = templateFields + (field.name to "")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            noteId == null && selectedTemplate != null -> "新建${selectedTemplate!!.name}"
                            noteId == null -> "新建笔记"
                            else -> "编辑笔记"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (noteId != null) {
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
                            val finalContent = if (selectedTemplate != null) {
                                templateFields.entries.joinToString("\n") { (key, value) ->
                                    val field = selectedTemplate!!.structure.fields.find { it.name == key }
                                    "${field?.label ?: key}: $value"
                                }
                            } else content

                            if (title.isNotBlank() || finalContent.isNotBlank()) {
                                if (noteId == null) {
                                    viewModel.createNote(title, finalContent, templateId)
                                } else {
                                    viewModel.updateNote(noteId, title, content, templateId, 0)
                                }
                                onNavigateBack()
                            }
                        },
                        enabled = (title.isNotBlank() || content.isNotBlank() || templateFields.values.any { it.isNotBlank() }) && !isLoading
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
                .verticalScroll(rememberScrollState())
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("标题") },
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Template fields or free content
            if (selectedTemplate != null) {
                selectedTemplate!!.structure.fields.forEach { field ->
                    OutlinedTextField(
                        value = templateFields[field.name] ?: "",
                        onValueChange = { value ->
                            templateFields = templateFields + (field.name to value)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(field.label) },
                        placeholder = { field.placeholder?.let { Text(it) } },
                        minLines = if (field.type == "text") 2 else 1
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    placeholder = { Text("记录你的灵感...") },
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI hint
            if (selectedTemplate?.systemPrompt != null) {
                Text(
                    text = "💡 此模板支持 AI 自动分类，保存后可点击 ✨ 按钮",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除笔记") },
            text = { Text("确定要删除这条灵感笔记吗？") },
            confirmButton = {
                TextButton(onClick = {
                    noteId?.let { viewModel.deleteNote(it) }
                    showDeleteDialog = false
                    onNavigateBack()
                }) {
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
