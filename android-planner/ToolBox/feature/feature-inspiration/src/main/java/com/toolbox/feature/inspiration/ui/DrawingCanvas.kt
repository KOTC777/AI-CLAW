package com.toolbox.feature.inspiration.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Drawing canvas for doodle/sketch functionality.
 */
@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    initialColor: Color = Color.Black,
    initialStrokeWidth: Float = 5f,
    onPathChanged: (List<DrawPath>) -> Unit = {}
) {
    var currentColor by remember { mutableStateOf(initialColor) }
    var currentStrokeWidth by remember { mutableStateOf(initialStrokeWidth) }
    val paths = remember { mutableStateListOf<DrawPath>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Column(modifier = modifier) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Color picker
            Row {
                val colors = listOf(Color.Black, Color.Red, Color.Blue, Color.Green, Color.Magenta)
                colors.forEach { color ->
                    Surface(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .padding(2.dp),
                        color = color,
                        shape = CircleShape,
                        onClick = { currentColor = color }
                    ) {}
                }
            }

            // Stroke width
            Row {
                listOf(3f, 5f, 10f, 15f).forEach { width ->
                    IconButton(
                        onClick = { currentStrokeWidth = width },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Canvas(modifier = Modifier.size(16.dp)) {
                            drawCircle(
                                color = if (currentStrokeWidth == width) Color.Gray else Color.LightGray,
                                radius = width / 2
                            )
                        }
                    }
                }
            }

            // Actions
            Row {
                IconButton(
                    onClick = {
                        if (paths.isNotEmpty()) {
                            paths.removeAt(paths.lastIndex)
                            onPathChanged(paths.toList())
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "撤销", modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = {
                        paths.clear()
                        onPathChanged(emptyList())
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "清除", modifier = Modifier.size(18.dp))
                }
            }
        }

        // Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPath = Path().apply { moveTo(offset.x, offset.y) }
                            currentPoints = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val offset = change.position
                            currentPath?.lineTo(offset.x, offset.y)
                            currentPoints = currentPoints + offset
                        },
                        onDragEnd = {
                            currentPath?.let { path ->
                                paths.add(DrawPath(path, currentColor, currentStrokeWidth))
                                onPathChanged(paths.toList())
                            }
                            currentPath = null
                            currentPoints = emptyList()
                        }
                    )
                }
        ) {
            // Draw saved paths
            paths.forEach { drawPath ->
                drawPath(
                    path = drawPath.path,
                    color = drawPath.color,
                    style = Stroke(
                        width = drawPath.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
            // Draw current path
            currentPath?.let { path ->
                drawPath(
                    path = path,
                    color = currentColor,
                    style = Stroke(
                        width = currentStrokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

data class DrawPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float
)
