package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.file.ZenFileManager
import com.example.ui.components.StorageProgressBar
import com.example.ui.components.formatBytesSize
import com.example.viewmodel.MainViewModel
import java.io.File

data class DashboardCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val folderName: String
)

@Composable
fun MainDashboard(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.storageStats.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val pinnedState by viewModel.pinnedFolders.collectAsState()
    val pinnedList = pinnedState.sortedBy { it.extraData.toIntOrNull() ?: it.id.toInt() }

    val storageRoot = ZenFileManager.getActiveStorageRoot(viewModel.getApplication())

    var showFolderPickerDialog by remember { mutableStateOf(false) }
    var dialogCurrentDir by remember { mutableStateOf(storageRoot) }

    val categories = listOf(
        DashboardCategory("Images", Icons.Filled.Image, Color(0xFF06B6D4), "Images"),
        DashboardCategory("Documents", Icons.Filled.Description, Color(0xFF3B82F6), "Documents"),
        DashboardCategory("Audio", Icons.Filled.AudioFile, Color(0xFF14B8A6), "Audio"),
        DashboardCategory("Videos", Icons.Filled.Movie, Color(0xFF8B5CF6), "Videos"),
        DashboardCategory("APKs", Icons.Filled.Android, Color(0xFFF59E0B), "APKs"),
        DashboardCategory("Downloads", Icons.Filled.Download, Color(0xFFEC4899), "Downloads"),
        DashboardCategory("Archives", Icons.Filled.FolderZip, Color(0xFF10B981), "Archives")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Welcoming Slogan
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = "Zen Xplorer",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Explore. Organize. Control.",
                    fontSize = 14.sp,
                    color = Color(0xFF06B6D4),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Storage Analyzer Snapshot Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E3A8A), Color(0xFF0A1F44))
                            )
                        )
                        .padding(20.dp)
                ) {
                    StorageProgressBar(stats = stats)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { viewModel.setScreen("storage") }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Optimize & Analyze Storage Space →",
                            color = Color(0xFF06B6D4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Categories Grid Title
        item {
            Text(
                text = "Smart File Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Grid contents
        item {
            Box(modifier = Modifier.height(290.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = false
                ) {
                    items(categories) { cat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(65.dp)
                                .clickable {
                                    val sandbox = ZenFileManager.getSandboxRoot(viewModel.getApplication())
                                    val targetFolder = File(sandbox, cat.folderName)
                                    viewModel.loadDirectory(targetFolder)
                                    viewModel.setScreen("files")
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(cat.color.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.title,
                                        tint = cat.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = cat.title,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    val sizeLabel = when (cat.title) {
                                        "Images" -> formatBytesSize(stats.imgsSize)
                                        "Documents" -> formatBytesSize(stats.docsSize)
                                        "Audio" -> formatBytesSize(stats.audioSize)
                                        "Videos" -> formatBytesSize(stats.vidsSize)
                                        "APKs" -> formatBytesSize(stats.apksSize)
                                        "Downloads" -> formatBytesSize(stats.docsSize) // Mimics similar allocations
                                        "Archives" -> formatBytesSize(stats.archivesSize)
                                        else -> "0 B"
                                    }
                                    Text(
                                        text = sizeLabel,
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // AI Advice quick-tip
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setScreen("ai") },
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF06B6D4))
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = "AI Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI File Optimization Suggestion",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "I found duplicate invoice downloads from last month. Clear up 45.5 MB now.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Quick Access Section & Customize/Pin Actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quick Access Folders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(
                    onClick = {
                        dialogCurrentDir = storageRoot
                        showFolderPickerDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Pin New Folder",
                        tint = Color(0xFF06B6D4)
                    )
                }
            }
        }

        if (pinnedList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No folders pinned yet.",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pin folders from files list or click the '+' button above to add quick access bookmarks here.",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            itemsIndexed(pinnedList) { index, pin ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val f = File(pin.originalPath)
                            if (f.exists() && f.isDirectory) {
                                viewModel.loadDirectory(f)
                                viewModel.setScreen("files")
                            } else {
                                viewModel.showMessage("Pinned folder does not exist or switch storage engine context!")
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFA855F7).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Folder,
                                contentDescription = "Pinned Folder",
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pin.name,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val storageRootText = storageRoot.absolutePath
                            val readablePath = pin.originalPath.replace(storageRootText, "ZenStorage")
                            Text(
                                text = readablePath,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.movePinnedFolderUp(pin) },
                                enabled = index > 0,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = if (index > 0) Color(0xFF06B6D4) else Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.movePinnedFolderDown(pin) },
                                enabled = index < pinnedList.size - 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = "Move Down",
                                    tint = if (index < pinnedList.size - 1) Color(0xFF06B6D4) else Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { viewModel.unpinFolder(pin.originalPath) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Unpin",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Favorites / Bookmark Row
        if (favorites.isNotEmpty()) {
            item {
                Text(
                    text = "Starred Bookmarks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(favorites) { fav ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val f = File(fav.originalPath)
                            if (f.exists() && f.isDirectory) {
                                viewModel.loadDirectory(f)
                                viewModel.setScreen("files")
                            } else {
                                viewModel.showMessage("File is bookmarked. Navigate inside the Files Explorer file viewer to launch!")
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F44).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (fav.isFolder) Icons.Filled.Folder else Icons.Filled.Description,
                            contentDescription = "Item Type",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fav.name,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Bookmark Path: ${fav.originalPath}",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { viewModel.toggleFavorite(
                            ZenFileManager.FileItem(
                                name = fav.name,
                                path = fav.originalPath,
                                isDirectory = fav.isFolder,
                                size = fav.size,
                                lastModified = fav.dateModified,
                                extension = fav.name.substringAfterLast('.', ""),
                                mimeType = fav.mimeType,
                                isFavorite = true
                            )
                        ) }) {
                            Icon(Icons.Filled.Star, contentDescription = "Unstar", tint = Color(0xFFF59E0B))
                        }
                    }
                }
            }
        }
    }

    if (showFolderPickerDialog) {
        val storageRoot = ZenFileManager.getActiveStorageRoot(viewModel.getApplication())
        val rootPath = storageRoot.absolutePath

        Dialog(onDismissRequest = { showFolderPickerDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F44)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Customize Quick Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Navigate directories below to select and pin folders for one-tap home access:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val canGoUp = dialogCurrentDir.absolutePath != rootPath
                        IconButton(
                            onClick = {
                                dialogCurrentDir.parentFile?.let { dialogCurrentDir = it }
                            },
                            enabled = canGoUp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Back",
                                tint = if (canGoUp) Color.White else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val cleanPath = dialogCurrentDir.absolutePath.replace(rootPath, "ZenStorage")
                        Text(
                            text = cleanPath,
                            fontSize = 12.sp,
                            color = Color(0xFF06B6D4),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val folders = dialogCurrentDir.listFiles()?.filter { it.isDirectory && !it.name.startsWith(".") } ?: emptyList()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.2f))
                    ) {
                        if (folders.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No subfolders in this directory",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(folders) { folder ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { dialogCurrentDir = folder }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Folder,
                                            contentDescription = "Folder",
                                            tint = Color(0xFFA855F7),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = folder.name,
                                            fontSize = 13.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showFolderPickerDialog = false },
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                viewModel.pinFolder(dialogCurrentDir)
                                showFolderPickerDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Pin Current Folder", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
