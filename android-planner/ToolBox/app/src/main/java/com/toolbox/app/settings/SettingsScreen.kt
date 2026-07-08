package com.toolbox.app.settings

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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.toolbox.core.security.vault.EmergencyDestroyer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    emergencyDestroyer: EmergencyDestroyer
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDestroyDialog by remember { mutableStateOf(false) }
    var showDestroySensitiveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Theme section
            SettingsSection(title = "外观") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "主题设置",
                    subtitle = "动态取色 / 暗色模式 / 自定义背景",
                    onClick = { /* TODO */ }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Security section
            SettingsSection(title = "安全") {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "修改主密码",
                    subtitle = "更改用于加密数据的主密码",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "应用锁设置",
                    subtitle = "配置生物识别 / PIN / 图案锁",
                    onClick = { /* TODO */ }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Sync section
            SettingsSection(title = "同步") {
                SettingsItem(
                    icon = Icons.Default.Sync,
                    title = "数据同步",
                    subtitle = "配置 WebDAV 或自建服务器同步",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    icon = Icons.Default.Sync,
                    title = "导入数据",
                    subtitle = "从 .vault 文件导入数据",
                    onClick = { /* TODO */ }
                )
                SettingsItem(
                    icon = Icons.Default.Sync,
                    title = "导出数据",
                    subtitle = "导出所有数据为加密 .vault 文件",
                    onClick = { /* TODO */ }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Danger zone
            SettingsSection(title = "危险操作") {
                SettingsItem(
                    icon = Icons.Default.Warning,
                    title = "清除敏感数据",
                    subtitle = "删除密码本和加密密钥，保留其他数据",
                    onClick = { showDestroySensitiveDialog = true },
                    isDestructive = true
                )
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "紧急销毁所有数据",
                    subtitle = "⚠️ 不可恢复！删除所有应用数据",
                    onClick = { showDestroyDialog = true },
                    isDestructive = true
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Version info
            Text(
                text = "ToolBox v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    // Destroy sensitive dialog
    if (showDestroySensitiveDialog) {
        AlertDialog(
            onDismissRequest = { showDestroySensitiveDialog = false },
            title = { Text("清除敏感数据") },
            text = { Text("这将删除密码本和所有加密密钥。其他数据（备忘录、日程等）将保留。\n\n此操作不可恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val result = emergencyDestroyer.destroySensitive()
                        when (result) {
                            is EmergencyDestroyer.DestroyResult.Success -> {
                                snackbarHostState.showSnackbar("敏感数据已清除")
                            }
                            is EmergencyDestroyer.DestroyResult.Error -> {
                                snackbarHostState.showSnackbar("错误: ${result.message}")
                            }
                        }
                    }
                    showDestroySensitiveDialog = false
                }) {
                    Text("确认清除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDestroySensitiveDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Destroy all dialog
    if (showDestroyDialog) {
        AlertDialog(
            onDismissRequest = { showDestroyDialog = false },
            title = { Text("⚠️ 紧急销毁") },
            text = {
                Text("这将永久删除所有数据，包括：\n\n• 所有备忘录\n• 所有日程\n• 所有打卡记录\n• 所有密码\n• 所有灵感笔记\n• 所有设置\n\n此操作绝对不可恢复！")
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val result = emergencyDestroyer.destroyAll()
                        when (result) {
                            is EmergencyDestroyer.DestroyResult.Success -> {
                                snackbarHostState.showSnackbar("所有数据已销毁")
                            }
                            is EmergencyDestroyer.DestroyResult.Error -> {
                                snackbarHostState.showSnackbar("错误: ${result.message}")
                            }
                        }
                    }
                    showDestroyDialog = false
                }) {
                    Text("确认销毁", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDestroyDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
