package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.file.ZenFileManager
import com.example.ui.components.AudioPlayerDialog
import com.example.ui.components.ImageViewerDialog
import com.example.ui.components.TextEditorDialog
import com.example.ui.components.formatBytesSize
import com.example.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentDir by viewModel.currentDir.collectAsState()
    val filesList by viewModel.files.collectAsState()
    val selectedPaths by viewModel.selectedPaths.collectAsState()
    val multiSelectMode by viewModel.multiSelectMode.collectAsState()
    val clipboardAction by viewModel.clipboardAction.collectAsState()
    val clipboardPaths by viewModel.clipboardPaths.collectAsState()
    val pinnedFolders by viewModel.pinnedFolders.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var useGridView by remember { mutableStateOf(false) }

    // Floating action states
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }
    
    // Bottom Sheet file details/options
    var activeOptionsItem by remember { mutableStateOf<ZenFileManager.FileItem?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // File rename state
    var showRenameDialog by remember { mutableStateOf<String?>(null) } // holds file path
    var renameInput by remember { mutableStateOf("") }

    // Zip archive creation dialog
    var showZipDialog by remember { mutableStateOf(false) }
    var zipNameInput by remember { mutableStateOf("") }

    // Media players trigger states
    var activeTextFile by remember { mutableStateOf<File?>(null) }
    var activeImageFile by remember { mutableStateOf<File?>(null) }
    var activeAudioFile by remember { mutableStateOf<File?>(null) }
    
    // Details viewer dialog
    var activeDetailedInfoFile by remember { mutableStateOf<ZenFileManager.FileItem?>(null) }

    val aiSearchResults by viewModel.aiSearchResults.collectAsState()
    val isAiSearching by viewModel.isAiSearching.collectAsState()
    var isAiSearchMode by remember { mutableStateOf(false) }

    // Filter results using client-side query or Gemini AI-powered natural language query:
    val filteredFiles = if (isAiSearchMode && aiSearchResults != null) {
        aiSearchResults!!
    } else {
        filesList.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            if (!multiSelectMode) {
                FloatingActionButton(
                    onClick = {
                        folderNameInput = ""
                        showCreateFolderDialog = true
                    },
                    containerColor = Color(0xFF3B82F6),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.CreateNewFolder, contentDescription = "New Folder")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // HEADER BAR (Breadcrumbs & Clipboard Actions)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A1F44).copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Clipboard paste indicators
                if (clipboardAction != null && clipboardPaths.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E3A8A))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.ContentPaste, contentDescription = "Active Clipboard", tint = Color(0xFF3B82F6))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Clipboard: ${clipboardPaths.size} items to ${clipboardAction}",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { viewModel.pasteClipboard() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Paste", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Batch Selection tools row
                if (multiSelectMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0E224E))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel selection", tint = Color.White)
                        }
                        Text(
                            text = "${selectedPaths.size} Selected",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.setClipboard("copy") }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Selected", tint = Color(0xFF06B6D4))
                        }
                        IconButton(onClick = { viewModel.setClipboard("move") }) {
                            Icon(Icons.Filled.ContentCut, contentDescription = "Cut Selected", tint = Color(0xFF8B5CF6))
                        }
                        IconButton(onClick = {
                            zipNameInput = "Archive"
                            showZipDialog = true
                        }) {
                            Icon(Icons.Filled.FolderZip, contentDescription = "Zip Selected", tint = Color(0xFF10B981))
                        }
                        IconButton(onClick = {
                            selectedPaths.forEach { viewModel.deleteFile(it) }
                            viewModel.clearSelection()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Trash Selected", tint = Color(0xFFEF4444))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Breadcrumbs Traversal Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val sandboxRoot = ZenFileManager.getSandboxRoot(LocalContext.current).absolutePath
                    val relativeToShow = currentDir.absolutePath.replace(sandboxRoot, "ZenStorage")
                        .replace(File.separator, "  ›  ")

                    IconButton(
                        onClick = { viewModel.navigateUp() },
                        enabled = currentDir.absolutePath != sandboxRoot
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back",
                            tint = if (currentDir.absolutePath != sandboxRoot) Color.White else Color(0xFF3A506B)
                        )
                    }

                    Text(
                        text = relativeToShow,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Compact/Grid Toggle
                    IconButton(onClick = { useGridView = !useGridView }) {
                        Icon(
                            imageVector = if (useGridView) Icons.Filled.CloseFullscreen else Icons.Filled.Folder,
                            contentDescription = "Structure Toggle",
                            tint = Color(0xFF06B6D4)
                        )
                    }
                }
            }

            // Client-side instant or AI Workspace Search
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Workspace Search Mode Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = if (isAiSearchMode) Color(0xFFA855F7).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = "Cognitive AI Mode",
                                tint = if (isAiSearchMode) Color(0xFFA855F7) else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Gemini Search Engine",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAiSearchMode) Color(0xFFA855F7) else Color.White
                            )
                            Text(
                                text = if (isAiSearchMode) "Ask raw questions to scan entire device" else "Using local directory filter",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Switch(
                        checked = isAiSearchMode,
                        onCheckedChange = { active ->
                            isAiSearchMode = active
                            if (!active) {
                                viewModel.clearAiSearch()
                            }
                        },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFA855F7),
                            checkedTrackColor = Color(0xFFA855F7).copy(alpha = 0.3f),
                            uncheckedThumbColor = Color(0xFF64748B),
                            uncheckedTrackColor = Color(0xFF0E224E)
                        ),
                        modifier = Modifier
                            .testTag("ai_search_switch")
                            .size(width = 44.dp, height = 24.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            if (it.isBlank() && isAiSearchMode) {
                                viewModel.clearAiSearch()
                            }
                        },
                        placeholder = { 
                            Text(
                                text = if (isAiSearchMode) "Ask Zen e.g. 'images under 1MB'..." else "Filter current files...", 
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                imageVector = if (isAiSearchMode) Icons.Filled.Psychology else Icons.Filled.Search, 
                                contentDescription = "Search icon", 
                                tint = if (isAiSearchMode) Color(0xFFA855F7) else Color(0xFF64748B)
                            ) 
                        },
                        trailingIcon = if (searchQuery.isNotBlank()) {
                            {
                                IconButton(
                                    onClick = { 
                                        if (isAiSearchMode) {
                                            viewModel.performAiSearch(searchQuery)
                                        } else {
                                            searchQuery = "" 
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isAiSearchMode) Icons.Filled.Search else Icons.Filled.Close, 
                                        contentDescription = "Search Action Button", 
                                        tint = if (isAiSearchMode) Color(0xFFA855F7) else Color(0xFF64748B)
                                    )
                                }
                            }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("workspace_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (isAiSearchMode) {
                                    viewModel.performAiSearch(searchQuery)
                                }
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isAiSearchMode) Color(0xFFA855F7) else Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF0E224E),
                            focusedContainerColor = Color(0xFF0E224E),
                            unfocusedContainerColor = Color(0xFF0E224E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    IconButton(
                        onClick = { viewModel.toggleStorageType() },
                        modifier = Modifier
                            .background(Color(0xFF0E224E), RoundedCornerShape(12.dp))
                            .testTag("storage_engine_toggle")
                    ) {
                        val storeType = ZenFileManager.getStorageType()
                        Icon(
                            imageVector = if (storeType == ZenFileManager.STORAGE_SANDBOX) Icons.Filled.Lock else Icons.Filled.Android,
                            contentDescription = "Engine Sandbox Toggle",
                            tint = if (storeType == ZenFileManager.STORAGE_SANDBOX) Color(0xFF06B6D4) else Color(0xFFF59E0B)
                        )
                    }
                }
            }

            // MAIN CONTENT (List or Grid)
            if (isAiSearching) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFA855F7),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Gemini Engine Searching...",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Traversing workspace database schemas & scanning indexes.",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else if (isAiSearchMode && aiSearchResults == null) {
                // AI search has not been run yet in this view
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Psychology,
                                contentDescription = "AI Core Instructions",
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Intelligent Semantic Search",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ask questions in natural language to search your entire storage device context powered by Gemini AI.",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "💡 Try asking:",
                                    color = Color(0xFFA855F7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "• \"find all files larger than 1MB\"\n• \"locate zipped folder items\"\n• \"pdf documents relating to notes\"\n• \"show raw audio tracks\"",
                                    color = Color(0xFF64748B),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            } else if (filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "No Files found",
                            tint = Color(0xFF3A506B),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isAiSearchMode) "No files matched your semantic query" else "Empty Directory or Search Query",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (useGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFiles) { item ->
                        val isSelected = selectedPaths.contains(item.path)
                        FileGridCard(
                            item = item,
                            isSelected = isSelected,
                            onTap = {
                                if (multiSelectMode) {
                                    viewModel.toggleSelection(item.path)
                                } else {
                                    handleFileClick(
                                        item = item,
                                        viewModel = viewModel,
                                        onTextOpen = { activeTextFile = it },
                                        onImgOpen = { activeImageFile = it },
                                        onAudioOpen = { activeAudioFile = it },
                                        onDetailsOpen = { activeDetailedInfoFile = it }
                                    )
                                }
                            },
                            onLongTap = {
                                viewModel.toggleSelection(item.path)
                            },
                            onOptions = { activeOptionsItem = item }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredFiles) { item ->
                        val isSelected = selectedPaths.contains(item.path)
                        FileListRow(
                            item = item,
                            isSelected = isSelected,
                            onTap = {
                                if (multiSelectMode) {
                                    viewModel.toggleSelection(item.path)
                                } else {
                                    handleFileClick(
                                        item = item,
                                        viewModel = viewModel,
                                        onTextOpen = { activeTextFile = it },
                                        onImgOpen = { activeImageFile = it },
                                        onAudioOpen = { activeAudioFile = it },
                                        onDetailsOpen = { activeDetailedInfoFile = it }
                                    )
                                }
                            },
                            onLongTap = {
                                viewModel.toggleSelection(item.path)
                            },
                            onOptions = { activeOptionsItem = item }
                        )
                    }
                }
            }
        }
    }

    // Modal bottom sheet triggers for file interactions
    if (activeOptionsItem != null) {
        val currItem = activeOptionsItem!!
        ModalBottomSheet(
            onDismissRequest = { activeOptionsItem = null },
            sheetState = sheetState,
            containerColor = Color(0xFF0E224E)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 8.dp)
                    .padding(horizontal = 24.dp)
            ) {
                // Header details
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getFileLogo(currItem.isDirectory, currItem.extension),
                        contentDescription = "Type",
                        tint = getFileColor(currItem.isDirectory, currItem.extension),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(currItem.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(formatBytesSize(currItem.size), color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions Column
                if (currItem.isDirectory) {
                    val isPinned = pinnedFolders.any { it.originalPath == currItem.path }
                    RowMenuItem(
                        label = if (isPinned) "Unpin Folder from Home" else "Pin Folder to Quick Access",
                        icon = Icons.Filled.Folder,
                        color = Color(0xFFA855F7)
                    ) {
                        viewModel.togglePinnedFolder(currItem)
                        activeOptionsItem = null
                    }
                }
                RowMenuItem(label = "Star / Favorite Bookmarks", icon = Icons.Filled.Star, color = Color(0xFFF59E0B)) {
                    viewModel.toggleFavorite(currItem)
                    activeOptionsItem = null
                }
                RowMenuItem(label = "Add to Secure Hidden Vault", icon = Icons.Filled.Lock, color = Color(0xFF06B6D4)) {
                    viewModel.moveToSecureVault(currItem)
                    activeOptionsItem = null
                }
                if (currItem.extension.lowercase() == "zip") {
                    RowMenuItem(label = "Extract ZIP Archive Here", icon = Icons.Filled.FolderZip, color = Color(0xFF10B981)) {
                        viewModel.extractZipFile(currItem)
                        activeOptionsItem = null
                    }
                }
                RowMenuItem(label = "Create Folder Summary (Zen AI)", icon = Icons.Filled.CheckCircle, color = Color(0xFF14B8A6)) {
                    if (currItem.isDirectory) {
                        viewModel.requestFolderSummary(currItem.path)
                    } else {
                        viewModel.showMessage("Folder summary is only applicable to Directories!")
                    }
                    activeOptionsItem = null
                }
                RowMenuItem(label = "Rename Item", icon = Icons.Filled.Edit, color = Color.White) {
                    renameInput = currItem.name
                    showRenameDialog = currItem.path
                    activeOptionsItem = null
                }
                RowMenuItem(label = "File details metadata", icon = Icons.Filled.Info, color = Color(0xFF3B82F6)) {
                    activeDetailedInfoFile = currItem
                    activeOptionsItem = null
                }
                RowMenuItem(label = "Move to Recycle Bin", icon = Icons.Filled.Delete, color = Color(0xFFEF4444)) {
                    viewModel.deleteFile(currItem.path)
                    activeOptionsItem = null
                }
            }
        }
    }

    // DIALOGS CORES
    if (showCreateFolderDialog) {
        Dialog(onDismissRequest = { showCreateFolderDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F44)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Create New Directory", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = folderNameInput,
                        onValueChange = { folderNameInput = it },
                        placeholder = { Text("Folder Name", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { showCreateFolderDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                if (folderNameInput.isNotBlank()) {
                                    viewModel.createFolder(folderNameInput)
                                }
                                showCreateFolderDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Text("Create", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog != null) {
        Dialog(onDismissRequest = { showRenameDialog = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F44)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Rename File or Directory", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { showRenameDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                if (renameInput.isNotBlank()) {
                                    viewModel.renameFile(showRenameDialog!!, renameInput)
                                }
                                showRenameDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Text("Rename", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showZipDialog) {
        Dialog(onDismissRequest = { showZipDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F44)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Compress to ZIP Archive", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = zipNameInput,
                        onValueChange = { zipNameInput = it },
                        placeholder = { Text("archive_name", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = { showZipDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                            Text("Cancel", color = Color(0xFF94A3B8))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = {
                                if (zipNameInput.isNotBlank()) {
                                    viewModel.archiveSelectedToZip(zipNameInput)
                                }
                                showZipDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                        ) {
                            Text("Compress", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (activeDetailedInfoFile != null) {
        val curr = activeDetailedInfoFile!!
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        Dialog(onDismissRequest = { activeDetailedInfoFile = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1F44)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Aesthetic File Metadata", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    MetadataRow(label = "Filename", value = curr.name)
                    MetadataRow(label = "Extension", value = curr.extension.uppercase())
                    MetadataRow(label = "Directory", value = if (curr.isDirectory) "Yes" else "No")
                    MetadataRow(label = "Size", value = "${curr.size} bytes (${formatBytesSize(curr.size)})")
                    MetadataRow(label = "Modified", value = formatter.format(Date(curr.lastModified)))
                    MetadataRow(label = "Canonical Path", value = curr.path)

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { activeDetailedInfoFile = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("Dimiss Metadata", color = Color.White)
                    }
                }
            }
        }
    }

    // Render active audio player
    if (activeAudioFile != null) {
        AudioPlayerDialog(file = activeAudioFile!!) {
            activeAudioFile = null
        }
    }

    // Render active image preview
    if (activeImageFile != null) {
        ImageViewerDialog(file = activeImageFile!!) {
            activeImageFile = null
        }
    }

    // Render active text textEditor
    if (activeTextFile != null) {
        TextEditorDialog(file = activeTextFile!!, onDismiss = { activeTextFile = null }) {
            viewModel.showMessage("Text document saved successfully")
            viewModel.loadDirectory(currentDir)
        }
    }
}

private fun handleFileClick(
    item: ZenFileManager.FileItem,
    viewModel: MainViewModel,
    onTextOpen: (File) -> Unit,
    onImgOpen: (File) -> Unit,
    onAudioOpen: (File) -> Unit,
    onDetailsOpen: (ZenFileManager.FileItem) -> Unit
) {
    val fileObj = File(item.path)
    if (item.isDirectory) {
        viewModel.loadDirectory(fileObj)
    } else {
        // Evaluate type player allocations
        val ext = item.extension.lowercase()
        when (ext) {
            "txt", "log", "json", "xml", "kt", "java" -> onTextOpen(fileObj)
            "jpg", "jpeg", "png", "webp", "gif" -> onImgOpen(fileObj)
            "mp3", "wav", "ogg", "m4a" -> onAudioOpen(fileObj)
            else -> onDetailsOpen(item)
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 13.sp, color = Color.White, modifier = Modifier.padding(top = 2.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileListRow(
    item: ZenFileManager.FileItem,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongTap: () -> Unit,
    onOptions: () -> Unit
) {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val formattedDate = formatter.format(Date(item.lastModified))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongTap
            )
            .background(if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.15f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Icon(Icons.Filled.CheckCircle, contentDescription = "Checked", tint = Color(0xFF06B6D4), modifier = Modifier.padding(end = 12.dp))
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(getFileColor(item.isDirectory, item.extension).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getFileLogo(item.isDirectory, item.extension),
                contentDescription = "File Type icon",
                tint = getFileColor(item.isDirectory, item.extension),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(modifier = Modifier.padding(top = 2.dp)) {
                Text(
                    text = if (item.isDirectory) "Folder" else formatBytesSize(item.size),
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        IconButton(onClick = onOptions) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Options context triggering",
                tint = Color(0xFF64748B)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridCard(
    item: ZenFileManager.FileItem,
    isSelected: Boolean,
    onTap: () -> Unit,
    onLongTap: () -> Unit,
    onOptions: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongTap
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF3B82F6).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(getFileColor(item.isDirectory, item.extension).copy(alpha = 0.1f), CircleShape)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getFileLogo(item.isDirectory, item.extension),
                    contentDescription = "Icon",
                    tint = getFileColor(item.isDirectory, item.extension),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.name,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = if (item.isDirectory) "Folder" else formatBytesSize(item.size),
                fontSize = 9.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
fun RowMenuItem(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

fun getFileLogo(isDirectory: Boolean, extension: String): ImageVector {
    if (isDirectory) return Icons.Filled.Folder
    return when (extension.lowercase()) {
        "txt", "log", "json", "xml" -> Icons.Filled.Description
        "jpg", "jpeg", "png", "webp", "gif" -> Icons.Filled.Image
        "mp3", "wav", "ogg", "m4a" -> Icons.Filled.AudioFile
        "mp4", "mkv", "avi", "mov" -> Icons.Filled.Movie
        "apk" -> Icons.Filled.Android
        "zip", "tar", "gz", "7z", "rar" -> Icons.Filled.FolderZip
        else -> Icons.Filled.QuestionMark
    }
}

fun getFileColor(isDirectory: Boolean, extension: String): Color {
    if (isDirectory) return Color(0xFFF59E0B) // Amber directories
    return when (extension.lowercase()) {
        "txt", "log", "json", "xml" -> Color(0xFF3B82F6) // Blue documents
        "jpg", "jpeg", "png", "webp", "gif" -> Color(0xFF06B6D4) // Cyan images
        "mp3", "wav", "ogg", "m4a" -> Color(0xFF14B8A6) // Teal audio
        "mp4", "mkv", "avi", "mov" -> Color(0xFF8B5CF6) // Purple video
        "apk" -> Color(0xFFF59E0B) // Yellow packages
        "zip", "tar", "gz", "7z", "rar" -> Color(0xFF10B981) // Green compression
        else -> Color(0xFF64748B) // Slate other
    }
}
