package com.toolbox.feature.checkin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.toolbox.core.common.extension.toFormattedDateTime
import com.toolbox.feature.checkin.data.CheckinTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinScreen(
    viewModel: CheckinViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val event by viewModel.event.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showCheckinDialog by remember { mutableStateOf<CheckinTask?>(null) }

    LaunchedEffect(event) {
        event?.let {
            when (it) {
                is CheckinEvent.Error -> snackbarHostState.showSnackbar(it.message)
                is CheckinEvent.TaskCreated -> snackbarHostState.showSnackbar("任务已创建")
                is CheckinEvent.CheckedIn -> snackbarHostState.showSnackbar("打卡成功！🎉")
            }
            viewModel.clearEvent()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务打卡") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "新建任务")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (tasks.isEmpty()) {
                EmptyCheckinState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onCheckin = { showCheckinDialog = task },
                            onArchive = { viewModel.archiveTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    // Create task dialog
    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, deadline, proofType ->
                viewModel.createTask(
                    title = title,
                    description = null,
                    deadlineTime = deadline,
                    intensityConfig = com.toolbox.feature.checkin.data.IntensityConfig(),
                    proofType = proofType
                )
                showCreateDialog = false
            }
        )
    }

    // Checkin dialog
    showCheckinDialog?.let { task ->
        CheckinDialog(
            task = task,
            onDismiss = { showCheckinDialog = null },
            onCheckin = { proofType, proofData ->
                viewModel.checkin(task.id, proofType, proofData)
                showCheckinDialog = null
            }
        )
    }
}

@Composable
private fun EmptyCheckinState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "✅", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "还没有打卡任务", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角 + 创建第一个任务",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskCard(
    task: CheckinTask,
    onCheckin: () -> Unit,
    onArchive: () -> Unit
) {
    Card(
        onClick = onCheckin,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row {
                    Text(
                        text = "🔥 ${task.currentStreak}天连续",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "总计 ${task.totalCheckins} 次",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                task.deadlineTime?.let {
                    Text(
                        text = "截止: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "打卡",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, deadline: String?, proofType: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("23:59") }
    var proofType by remember { mutableStateOf("text") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建打卡任务") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("任务名称") },
                    placeholder = { Text("例如：早起、运动、阅读...") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = deadline,
                    onValueChange = { deadline = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("截止时间") },
                    placeholder = { Text("HH:mm") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "证明方式: $proofType",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row {
                    listOf("text", "photo", "audio").forEach { type ->
                        TextButton(onClick = { proofType = type }) {
                            Text(type)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(title, deadline, proofType) },
                enabled = title.isNotBlank()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
