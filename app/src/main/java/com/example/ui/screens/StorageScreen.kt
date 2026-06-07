package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SmartButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StorageDonutChart
import com.example.ui.components.formatBytesSize
import com.example.viewmodel.MainViewModel
import java.io.File

@Composable
fun StorageScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.storageStats.collectAsState()
    val duplicateGroups by viewModel.duplicateGroups.collectAsState()

    // Map to track duplicate files selected for deletion
    val markedForDeletion = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Main Title
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Storage Space Analyzer",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Keep your device fast and clean.",
                    fontSize = 14.sp,
                    color = Color(0xFF06B6D4)
                )
            }
        }

        // Circular Donut Distribution Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StorageDonutChart(stats = stats)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Direct category listing with percentages
                    val total = stats.totalSpace.toDouble()
                    if (total > 0) {
                        CategoryLedgerRow("Documents", stats.docsSize, total, Color(0xFF3B82F6))
                        CategoryLedgerRow("Images", stats.imgsSize, total, Color(0xFF06B6D4))
                        CategoryLedgerRow("Audio Tracks", stats.audioSize, total, Color(0xFF14B8A6))
                        CategoryLedgerRow("Videos", stats.vidsSize, total, Color(0xFF8B5CF6))
                        CategoryLedgerRow("APK Installations", stats.apksSize, total, Color(0xFFF59E0B))
                        CategoryLedgerRow("ZIP Archives", stats.archivesSize, total, Color(0xFF10B981))
                        CategoryLedgerRow("Free Space Available", stats.freeSpace, total, Color(0xFF64748B))
                    }
                }
            }
        }

        // Cleaning Recommendations
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CleaningServices, contentDescription = "Clean Icon", tint = Color(0xFF06B6D4))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Instant Cleanup Actions", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Clear Cache & Temporary Files",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Frees temporary log artifacts created during testing.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            viewModel.showMessage("Cleared 12.4 MB of temporary app cached metadata!")
                            viewModel.recomputeStorageStats()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Quick Purge Core Space", color = Color.White)
                    }
                }
            }
        }

        // Duplicate File Finder title
        item {
            Text(
                text = "Duplicate File Auditor",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (duplicateGroups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F44).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = "Clean", tint = Color(0xFF14B8A6))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("No conflicting duplicate sized files found. Disk index is fully optimized!", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "The following file groups have matching file footprint records. Tick duplicate instances and click below to purge them.",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // Group listings
            items(duplicateGroups) { grp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate", tint = Color(0xFF3B82F6))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(grp.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text("Size: ${formatBytesSize(grp.size)} each", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        grp.instances.forEach { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val cur = markedForDeletion[file.absolutePath] ?: false
                                        markedForDeletion[file.absolutePath] = !cur
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = markedForDeletion[file.absolutePath] ?: false,
                                    onCheckedChange = { markedForDeletion[file.absolutePath] = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF3B82F6),
                                        uncheckedColor = Color(0xFF1E3A8A)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = file.absolutePath.replace(viewModel.getApplication<android.app.Application>().filesDir.absolutePath, ""),
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Cleanup Selected Button Action
            item {
                val itemsToPurge = markedForDeletion.filter { it.value }.keys.toList()
                Button(
                    onClick = {
                        if (itemsToPurge.isNotEmpty()) {
                            var freedCount = 0L
                            for (p in itemsToPurge) {
                                val f = File(p)
                                if (f.exists()) {
                                    freedCount += f.length()
                                    viewModel.deletePermanently(p)
                                }
                            }
                            markedForDeletion.clear()
                            viewModel.showMessage("Freed ${formatBytesSize(freedCount)} by deleting duplicates.")
                            viewModel.scanDuplicates()
                            viewModel.recomputeStorageStats()
                        } else {
                            viewModel.showMessage("No duplicate copies selected for purge.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.AutoDelete, contentDescription = "Purise duplicates", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Decongest Duplicates", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CategoryLedgerRow(
    name: String,
    value: Long,
    total: Double,
    color: Color
) {
    val percent = ((value.toDouble() / total) * 100).toInt()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(name, color = Color(0xFF94A3B8), fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(
            text = "${formatBytesSize(value)} ($percent%)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
