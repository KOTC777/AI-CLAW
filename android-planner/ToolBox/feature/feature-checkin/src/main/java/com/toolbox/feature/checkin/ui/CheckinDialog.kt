package com.toolbox.feature.checkin.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toolbox.feature.checkin.data.CheckinTask

@Composable
fun CheckinDialog(
    task: CheckinTask,
    onDismiss: () -> Unit,
    onCheckin: (proofType: String, proofData: String?) -> Unit
) {
    var textProof by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "打卡: ${task.title}",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    text = "请提交打卡证明",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (task.proofType) {
                    "text" -> {
                        OutlinedTextField(
                            value = textProof,
                            onValueChange = { textProof = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("打卡内容") },
                            placeholder = { Text("记录今天的打卡内容...") },
                            minLines = 3
                        )
                    }
                    "photo" -> {
                        Text(
                            text = "📷 请拍照作为打卡证明",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        // TODO: Camera integration
                        Text(
                            text = "（相机功能将在后续版本实现）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    "audio" -> {
                        Text(
                            text = "🎤 请录音作为打卡证明",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "（录音功能将在后续版本实现）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "证明提交后将停止今日提醒",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCheckin(
                        task.proofType,
                        if (task.proofType == "text") textProof else "submitted"
                    )
                },
                enabled = when (task.proofType) {
                    "text" -> textProof.isNotBlank()
                    else -> true
                }
            ) {
                Text("确认打卡")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
