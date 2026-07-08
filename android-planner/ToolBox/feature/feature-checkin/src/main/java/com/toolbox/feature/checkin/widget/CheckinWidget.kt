package com.toolbox.feature.checkin.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/**
 * Checkin widget for home screen.
 * Shows tasks with checkin status.
 */
class CheckinWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // TODO: Load tasks from database
        val tasks = listOf(
            WidgetTask("早起", 7, false),
            WidgetTask("运动", 3, true),
            WidgetTask("阅读", 12, false)
        )

        provideContent {
            GlanceTheme {
                CheckinWidgetContent(tasks)
            }
        }
    }
}

@Composable
private fun CheckinWidgetContent(tasks: List<WidgetTask>) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.surface)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✅",
                style = TextStyle(fontSize = 20.sp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "今日打卡",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        if (tasks.isEmpty()) {
            Text(
                text = "暂无打卡任务",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        } else {
            tasks.forEach { task ->
                TaskWidgetItem(task)
                Spacer(modifier = GlanceModifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun TaskWidgetItem(task: WidgetTask) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(8.dp)
            .background(GlanceTheme.colors.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator
        Text(
            text = if (task.checkedIn) "✅" else "⬜",
            style = TextStyle(fontSize = 16.sp)
        )

        Spacer(modifier = GlanceModifier.width(12.dp))

        // Task info
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = task.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                )
            )
            Text(
                text = "🔥 ${task.streak}天连续",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = if (task.checkedIn)
                        ColorProvider(android.graphics.Color.parseColor("#4CAF50"))
                    else GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

data class WidgetTask(
    val title: String,
    val streak: Int,
    val checkedIn: Boolean
)

class CheckinWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CheckinWidget()
}
