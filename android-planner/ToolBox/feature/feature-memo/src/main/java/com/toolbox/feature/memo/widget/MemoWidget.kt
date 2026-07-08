package com.toolbox.feature.memo.widget

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
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
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
 * Memo widget for home screen.
 * Shows recent memos in a list.
 */
class MemoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // TODO: Load memos from database
        val memos = listOf(
            WidgetMemo("示例备忘录", "点击打开应用查看", "刚刚"),
        )

        provideContent {
            GlanceTheme {
                MemoWidgetContent(memos)
            }
        }
    }
}

@Composable
private fun MemoWidgetContent(memos: List<WidgetMemo>) {
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
                text = "📝",
                style = TextStyle(fontSize = 20.sp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "备忘录",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        if (memos.isEmpty()) {
            Text(
                text = "暂无备忘录",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        } else {
            LazyColumn {
                items(memos) { memo ->
                    MemoWidgetItem(memo)
                    Spacer(modifier = GlanceModifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun MemoWidgetItem(memo: WidgetMemo) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(8.dp)
            .background(GlanceTheme.colors.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(
            text = memo.title,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurface
            ),
            maxLines = 1
        )
        if (memo.preview.isNotBlank()) {
            Text(
                text = memo.preview,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 2
            )
        }
        Text(
            text = memo.time,
            style = TextStyle(
                fontSize = 10.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
    }
}

data class WidgetMemo(
    val title: String,
    val preview: String,
    val time: String
)

/**
 * Widget receiver for manifest registration.
 */
class MemoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MemoWidget()
}
