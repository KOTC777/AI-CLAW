package com.toolbox.feature.schedule.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.toolbox.feature.schedule.data.ReminderConfig
import com.toolbox.feature.schedule.data.RepeatRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    eventId: String?,
    initialDate: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var allDay by remember { mutableStateOf(false) }
    var selectedRepeatType by remember { mutableStateOf("none") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var reminderMinutes by remember { mutableIntStateOf(15) }
    var reminderIntensity by remember { mutableIntStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == null) "新建日程" else "编辑日程") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (eventId != null) {
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
                            if (title.isNotBlank()) {
                                val now = initialDate ?: System.currentTimeMillis()
                                viewModel.createEvent(
                                    title = title,
                                    description = description.ifBlank { null },
                                    startTime = now,
                                    endTime = now + 3600_000,
                                    allDay = allDay,
                                    repeatRule = if (selectedRepeatType != "none") {
                                        RepeatRule(type = selectedRepeatType)
                                    } else null,
                                    reminderConfig = if (reminderEnabled) {
                                        ReminderConfig(
                                            enabled = true,
                                            minutesBefore = listOf(reminderMinutes),
                                            intensityLevel = reminderIntensity
                                        )
                                    } else null,
                                    color = null
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank() && !isLoading
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
                label = { Text("标题") },
                placeholder = { Text("日程标题...") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("描述") },
                placeholder = { Text("可选描述...") },
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            // All day switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "全天",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = allDay,
                    onCheckedChange = { allDay = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat
            Text(
                text = "重复",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val repeatOptions = listOf(
                    "none" to "不重复",
                    "daily" to "每天",
                    "weekly" to "每周",
                    "monthly" to "每月",
                    "yearly" to "每年"
                )
                repeatOptions.forEach { (type, label) ->
                    FilterChip(
                        selected = selectedRepeatType == type,
                        onClick = { selectedRepeatType = type },
                        label = { Text(label) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminder
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "提醒",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it }
                )
            }

            if (reminderEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                // Minutes before
                Text(
                    text = "提前 ${reminderMinutes} 分钟提醒",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val minuteOptions = listOf(5, 15, 30, 60)
                    minuteOptions.forEach { minutes ->
                        FilterChip(
                            selected = reminderMinutes == minutes,
                            onClick = { reminderMinutes = minutes },
                            label = { Text("${minutes}分钟") }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Intensity
                Text(
                    text = "提醒强度",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { level ->
                        FilterChip(
                            selected = reminderIntensity == level,
                            onClick = { reminderIntensity = level },
                            label = { Text("L$level") }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Delete dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除日程") },
            text = { Text("确定要删除这个日程吗？") },
            confirmButton = {
                TextButton(onClick = {
                    eventId?.let { viewModel.deleteEvent(it) }
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
