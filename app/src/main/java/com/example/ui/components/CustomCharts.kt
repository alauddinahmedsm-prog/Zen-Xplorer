package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel

data class ChartSegment(
    val name: String,
    val value: Long,
    val color: Color
)

@Composable
fun StorageDonutChart(
    stats: MainViewModel.StorageStats,
    modifier: Modifier = Modifier
) {
    val total = stats.totalSpace.toDouble()
    if (total <= 0) return

    val segments = listOf(
        ChartSegment("Docs", stats.docsSize, Color(0xFF3B82F6)),
        ChartSegment("Images", stats.imgsSize, Color(0xFF06B6D4)),
        ChartSegment("Audio", stats.audioSize, Color(0xFF14B8A6)),
        ChartSegment("Videos", stats.vidsSize, Color(0xFF8B5CF6)),
        ChartSegment("APKs", stats.apksSize, Color(0xFFF59E0B)),
        ChartSegment("Zips", stats.archivesSize, Color(0xFFEF4444)),
        ChartSegment("Others", stats.othersSize, Color(0xFF64748B)),
        ChartSegment("Free", stats.freeSpace, Color(0xFF1E293B))
    ).filter { it.value > 0 }

    var animationTrigger by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "donut_chart_anim"
    )

    LaunchedEffect(stats) {
        animationTrigger = true
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val strokeWidth = 24.dp.toPx()
            val canvasSize = size
            val width = canvasSize.width
            val height = canvasSize.height
            val radius = (width - strokeWidth) / 2f

            var startAngle = -90f

            for (seg in segments) {
                val sweepAngle = ((seg.value.toDouble() / total) * 360f).toFloat() * animatedProgress
                
                drawArc(
                    color = seg.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(width - strokeWidth, height - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }

        // Center labels
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val usedPercent = ((stats.usedSpace.toDouble() / total) * 100).toInt()
            Text(
                text = "$usedPercent%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Used Storage",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun LegendItem(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StorageProgressBar(
    stats: MainViewModel.StorageStats,
    modifier: Modifier = Modifier
) {
    val total = stats.totalSpace.toDouble()
    if (total <= 0) return

    val usedPercent = ((stats.usedSpace.toDouble() / total) * 100).toInt()

    val mediaSize = (stats.imgsSize + stats.vidsSize + stats.audioSize).toDouble()
    val appsSize = stats.apksSize.toDouble()
    val documentsSize = stats.docsSize.toDouble()
    val othersAndSystemSize = (stats.usedSpace - mediaSize - appsSize - documentsSize).coerceAtLeast(0.0)

    val mediaAndDocsRatio = if (total > 0) ((mediaSize + documentsSize) / total).toFloat() else 0f
    val appsRatio = if (total > 0) (appsSize / total).toFloat() else 0f
    val systemRatio = if (total > 0) (othersAndSystemSize / total).toFloat() else 0f

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Total Storage",
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF93C5FD), // text-blue-300
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatBytesSize(stats.usedSpace) + " ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "/ " + formatBytesSize(stats.totalSpace),
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(Color(0xFF3B82F6).copy(alpha = 0.2f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$usedPercent% Used",
                    color = Color(0xFF60A5FA), // light-blue
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Multi-segment progress bar with weight
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color(0xFF0F172A).copy(alpha = 0.5f), shape = CircleShape)
        ) {
            val totalRatio = mediaAndDocsRatio + appsRatio + systemRatio
            if (totalRatio > 0) {
                if (mediaAndDocsRatio > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(mediaAndDocsRatio.coerceAtLeast(0.01f))
                            .background(Color(0xFF3B82F6))
                    )
                }
                if (appsRatio > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(appsRatio.coerceAtLeast(0.01f))
                            .background(Color(0xFFA855F7))
                    )
                }
                if (systemRatio > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(systemRatio.coerceAtLeast(0.01f))
                            .background(Color(0xFF14B8A6))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegendItem(name = "Media & Docs", color = Color(0xFF3B82F6))
            LegendItem(name = "Apps", color = Color(0xFFA855F7))
            LegendItem(name = "System", color = Color(0xFF14B8A6))
        }
    }
}

fun formatBytesSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
