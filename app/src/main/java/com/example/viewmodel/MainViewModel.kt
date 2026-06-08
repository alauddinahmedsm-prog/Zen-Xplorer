package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.FileMetaEntity
import com.example.data.file.ZenFileManager
import com.example.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"

    private val db = AppDatabase.getInstance(application)
    private val repository = FileRepository(application, db.fileMetaDao)

    // Current app state navigation
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // File list state
    private val _currentDir = MutableStateFlow<File>(ZenFileManager.getActiveStorageRoot(application))
    val currentDir: StateFlow<File> = _currentDir.asStateFlow()

    private val _files = MutableStateFlow<List<ZenFileManager.FileItem>>(emptyList())
    val files: StateFlow<List<ZenFileManager.FileItem>> = _files.asStateFlow()

    // Selection status
    private val _selectedPaths = MutableStateFlow<Set<String>>(emptySet())
    val selectedPaths: StateFlow<Set<String>> = _selectedPaths.asStateFlow()

    private val _multiSelectMode = MutableStateFlow(false)
    val multiSelectMode: StateFlow<Boolean> = _multiSelectMode.asStateFlow()

    // Clipboard operation state: "copy" or "move" or null paired with paths
    private val _clipboardAction = MutableStateFlow<String?>(null) // "copy", "move"
    val clipboardAction: StateFlow<String?> = _clipboardAction.asStateFlow()

    private val _clipboardPaths = MutableStateFlow<Set<String>>(emptySet())
    val clipboardPaths: StateFlow<Set<String>> = _clipboardPaths.asStateFlow()

    // Keyboard shortcuts & Focus helpers
    private val _searchFocusTrigger = MutableStateFlow(0)
    val searchFocusTrigger: StateFlow<Int> = _searchFocusTrigger.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    // Database reactive bindings
    val favorites: StateFlow<List<FileMetaEntity>> = repository.favoriteFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recycleBin: StateFlow<List<FileMetaEntity>> = repository.recycleBinFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultFiles: StateFlow<List<FileMetaEntity>> = repository.vaultFiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedFolders: StateFlow<List<FileMetaEntity>> = repository.pinnedFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Theme configuration: true = dark premium, false = alternate
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    // Cloud integration states and sync preferences
    private val _isGoogleDriveConnected = MutableStateFlow(false)
    val isGoogleDriveConnected: StateFlow<Boolean> = _isGoogleDriveConnected.asStateFlow()

    private val _isOneDriveConnected = MutableStateFlow(false)
    val isOneDriveConnected: StateFlow<Boolean> = _isOneDriveConnected.asStateFlow()

    private val _isDropboxConnected = MutableStateFlow(false)
    val isDropboxConnected: StateFlow<Boolean> = _isDropboxConnected.asStateFlow()

    private val _isSftpConnected = MutableStateFlow(false)
    val isSftpConnected: StateFlow<Boolean> = _isSftpConnected.asStateFlow()

    private val defaultSyncMap = mapOf(
        "Documents" to true,
        "Images" to true,
        "Audio" to true,
        "Videos" to true,
        "APKs" to true,
        "Downloads" to true,
        "Archives" to true
    )

    private val _googleDriveSyncFolders = MutableStateFlow(defaultSyncMap)
    val googleDriveSyncFolders: StateFlow<Map<String, Boolean>> = _googleDriveSyncFolders.asStateFlow()

    private val _oneDriveSyncFolders = MutableStateFlow(defaultSyncMap)
    val oneDriveSyncFolders: StateFlow<Map<String, Boolean>> = _oneDriveSyncFolders.asStateFlow()

    // Security constraints
    private val _appPin = MutableStateFlow("1234") // Default setup PIN
    val appPin: StateFlow<String> = _appPin.asStateFlow()

    private val prefs = application.getSharedPreferences("zen_prefs", Context.MODE_PRIVATE)

    private val _welcomeCompleted = MutableStateFlow(prefs.getBoolean("welcome_completed", false))
    val welcomeCompleted: StateFlow<Boolean> = _welcomeCompleted.asStateFlow()

    fun completeWelcome() {
        prefs.edit().putBoolean("welcome_completed", true).apply()
        _welcomeCompleted.value = true
    }

    private val _isAppLockEnabled = MutableStateFlow(prefs.getBoolean("app_lock_enabled", false))
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    fun setAppLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("app_lock_enabled", enabled).apply()
        _isAppLockEnabled.value = enabled
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _showHiddenFiles = MutableStateFlow(prefs.getBoolean("show_hidden_files", false))
    val showHiddenFiles: StateFlow<Boolean> = _showHiddenFiles.asStateFlow()

    fun setShowHiddenFiles(enabled: Boolean) {
        prefs.edit().putBoolean("show_hidden_files", enabled).apply()
        _showHiddenFiles.value = enabled
        loadDirectory(_currentDir.value)
    }

    private val _safTreeUri = MutableStateFlow(prefs.getString("saf_tree_uri", null))
    val safTreeUri: StateFlow<String?> = _safTreeUri.asStateFlow()

    fun saveSafTreeUri(uri: String) {
        prefs.edit().putString("saf_tree_uri", uri).apply()
        _safTreeUri.value = uri
        loadDirectory(_currentDir.value)
    }

    fun clearSafTreeUri() {
        prefs.edit().remove("saf_tree_uri").apply()
        _safTreeUri.value = null
        loadDirectory(_currentDir.value)
    }

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _storageError = MutableStateFlow<String?>(null)
    val storageError: StateFlow<String?> = _storageError.asStateFlow()

    private val _isVaultLocked = MutableStateFlow(true)
    val isVaultLocked: StateFlow<Boolean> = _isVaultLocked.asStateFlow()

    // Storage analytical states
    data class StorageStats(
        val totalSpace: Long = 100 * 1024 * 1024, // Simulated default size in bytes
        val freeSpace: Long = 50 * 1024 * 1024,
        val docsSize: Long = 0,
        val imgsSize: Long = 0,
        val audioSize: Long = 0,
        val vidsSize: Long = 0,
        val apksSize: Long = 0,
        val archivesSize: Long = 0,
        val othersSize: Long = 0
    ) {
        val usedSpace: Long get() = totalSpace - freeSpace
    }

    private val _storageStats = MutableStateFlow(StorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    // AI Chat logs
    data class ChatMessage(val content: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("Greetings! I am Zen, the primary cognitive core of Zen Xplorer. How can I assist you with searching, archiving, or auditing security today?", false)
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    // AI Semantic and natural language search state for Files screen
    private val _aiSearchResults = MutableStateFlow<List<ZenFileManager.FileItem>?>(null)
    val aiSearchResults: StateFlow<List<ZenFileManager.FileItem>?> = _aiSearchResults.asStateFlow()

    private val _isAiSearching = MutableStateFlow(false)
    val isAiSearching: StateFlow<Boolean> = _isAiSearching.asStateFlow()

    // Duplicates and cleaner statistics
    data class DuplicateGroup(val name: String, val size: Long, val instances: List<File>)
    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups.asStateFlow()

    // Toast/Snackbar notifications
    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        // Prepare sandbox
        ZenFileManager.initializeSandboxIfNeeded(application)
        loadDirectory(ZenFileManager.getActiveStorageRoot(application))
        recomputeStorageStats()
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
        if (screen == "storage") {
            recomputeStorageStats()
            scanDuplicates()
        }
    }

    fun showMessage(msg: String) {
        _operationMessage.value = msg
    }

    fun clearMessage() {
        _operationMessage.value = null
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setAppLocked(locked: Boolean) {
        _isAppLocked.value = locked
    }

    fun checkAppPin(pin: String): Boolean {
        return if (pin == _appPin.value) {
            _isAppLocked.value = false
            true
        } else false
    }

    fun changeAppPin(oldPin: String, newPin: String): Boolean {
        return if (oldPin == _appPin.value) {
            _appPin.value = newPin
            true
        } else false
    }

    fun unlockVault(pin: String): Boolean {
        return if (pin == _appPin.value) {
            _isVaultLocked.value = false
            true
        } else false
    }

    fun unlockVaultWithoutPin() {
        _isVaultLocked.value = false
    }

    fun lockVault() {
        _isVaultLocked.value = true
    }

    // Cloud connection handlers
    fun setGoogleDriveConnected(connected: Boolean) {
        _isGoogleDriveConnected.value = connected
    }

    fun setOneDriveConnected(connected: Boolean) {
        _isOneDriveConnected.value = connected
    }

    fun setDropboxConnected(connected: Boolean) {
        _isDropboxConnected.value = connected
    }

    fun setSftpConnected(connected: Boolean) {
        _isSftpConnected.value = connected
    }

    fun toggleGoogleDriveSyncFolder(folder: String) {
        val current = _googleDriveSyncFolders.value.toMutableMap()
        current[folder] = !(current[folder] ?: true)
        _googleDriveSyncFolders.value = current
        showMessage("Google Drive: Sync for '$folder' ${if (current[folder] == true) "enabled" else "disabled"}")
    }

    fun toggleOneDriveSyncFolder(folder: String) {
        val current = _oneDriveSyncFolders.value.toMutableMap()
        current[folder] = !(current[folder] ?: true)
        _oneDriveSyncFolders.value = current
        showMessage("OneDrive: Sync for '$folder' ${if (current[folder] == true) "enabled" else "disabled"}")
    }

    // Keyboard shortcut operations
    fun triggerSearchFocus() {
        _searchFocusTrigger.value = _searchFocusTrigger.value + 1
    }

    fun showDeleteConfirmationPrompt() {
        if (_selectedPaths.value.isNotEmpty()) {
            _showDeleteConfirmation.value = true
        } else {
            showMessage("Select files to delete first")
        }
    }

    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    fun confirmAndExecuteDeletion() {
        _showDeleteConfirmation.value = false
        viewModelScope.launch {
            val paths = _selectedPaths.value
            if (paths.isNotEmpty()) {
                paths.forEach { path ->
                    val file = File(path)
                    repository.moveToRecycleBin(file)
                }
                showMessage("Moved ${paths.size} item(s) to Recycle Bin")
                _selectedPaths.value = emptySet()
                _multiSelectMode.value = false
                loadDirectory(_currentDir.value)
                recomputeStorageStats()
            }
        }
    }

    /**
     * Directory loading
     */
    fun loadDirectory(dir: File) {
        val app = getApplication<android.app.Application>()
        val isDeviceStorage = ZenFileManager.getStorageType() == ZenFileManager.STORAGE_DEVICE
        val hasStoragePerm = com.example.data.permission.PermissionManager.hasStoragePermission(app)

        if (isDeviceStorage && !hasStoragePerm) {
            // Do not scan or look at files before permission is granted
            _files.value = emptyList()
            _selectedPaths.value = emptySet()
            _multiSelectMode.value = false
            _storageError.value = "Storage permission required"
            return
        }

        _isScanning.value = true
        _storageError.value = null
        _currentDir.value = dir

        viewModelScope.launch {
            try {
                if (isDeviceStorage) {
                    if (!dir.exists()) {
                        try {
                            if (!dir.mkdirs()) {
                                _storageError.value = "Storage pathway is unavailable: ${dir.name}"
                                _files.value = emptyList()
                                return@launch
                            }
                        } catch (securityEx: SecurityException) {
                            _storageError.value = "Access denied: ${securityEx.localizedMessage}"
                            _files.value = emptyList()
                            return@launch
                        }
                    }
                } else {
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                }

                val fileItems = ZenFileManager.listFilesInDirectory(app, dir)
                val updatedItems = fileItems.map { item ->
                    item.copy(isFavorite = repository.isFavorite(item.path))
                }
                _files.value = updatedItems
                _selectedPaths.value = emptySet()
                _multiSelectMode.value = false
            } catch (e: Exception) {
                _storageError.value = "Failed accessing directory structure: ${e.localizedMessage}"
                _files.value = emptyList()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun navigateUp(): Boolean {
        val rootPath = ZenFileManager.getActiveStorageRoot(getApplication()).absolutePath
        val currentPath = _currentDir.value.absolutePath
        if (currentPath == rootPath) {
            return false // Already at storage root boundary
        }
        _currentDir.value.parentFile?.let { parent ->
            loadDirectory(parent)
            return true
        }
        return false
    }

    fun toggleStorageType() {
        val app = getApplication<Application>()
        if (ZenFileManager.getStorageType() == ZenFileManager.STORAGE_SANDBOX) {
            ZenFileManager.setStorageType(ZenFileManager.STORAGE_DEVICE)
            showMessage("Switched to Device Internal Storage")
        } else {
            ZenFileManager.setStorageType(ZenFileManager.STORAGE_SANDBOX)
            showMessage("Switched to Local Sandbox Storage")
        }
        loadDirectory(ZenFileManager.getActiveStorageRoot(app))
        recomputeStorageStats()
    }

    /**
     * File Management actions
     */
    fun toggleFavorite(fileItem: ZenFileManager.FileItem) {
        viewModelScope.launch {
            val file = File(fileItem.path)
            if (fileItem.isFavorite) {
                repository.removeFavorite(fileItem.path)
                showMessage("Removed from Favorites")
            } else {
                repository.addFavorite(file)
                showMessage("Added to Favorites")
            }
            // Reload
            loadDirectory(_currentDir.value)
        }
    }

    fun togglePinnedFolder(fileItem: ZenFileManager.FileItem) {
        viewModelScope.launch {
            val file = File(fileItem.path)
            if (repository.isPinned(fileItem.path)) {
                repository.removePinnedFolder(fileItem.path)
                showMessage("Unpinned '${file.name}' from Quick Access")
            } else {
                repository.addPinnedFolder(file)
                showMessage("Pinned '${file.name}' to Quick Access")
            }
            // Reload
            loadDirectory(_currentDir.value)
        }
    }

    fun pinFolder(file: File) {
        viewModelScope.launch {
            repository.addPinnedFolder(file)
            showMessage("Pinned '${file.name}' to Quick Access")
            loadDirectory(_currentDir.value)
        }
    }

    fun unpinFolder(path: String) {
        viewModelScope.launch {
            repository.removePinnedFolder(path)
            showMessage("Unpinned folder from Quick Access")
            loadDirectory(_currentDir.value)
        }
    }

    fun movePinnedFolderUp(entity: FileMetaEntity) {
        viewModelScope.launch {
            val sortedList = pinnedFolders.value.sortedBy { it.extraData.toIntOrNull() ?: it.id.toInt() }
            val index = sortedList.indexOfFirst { it.id == entity.id }
            if (index > 0) {
                repository.reorderPinnedFolder(index, index - 1)
                showMessage("Moved '${entity.name}' up")
            }
        }
    }

    fun movePinnedFolderDown(entity: FileMetaEntity) {
        viewModelScope.launch {
            val sortedList = pinnedFolders.value.sortedBy { it.extraData.toIntOrNull() ?: it.id.toInt() }
            val index = sortedList.indexOfFirst { it.id == entity.id }
            if (index >= 0 && index < sortedList.size - 1) {
                repository.reorderPinnedFolder(index, index + 1)
                showMessage("Moved '${entity.name}' down")
            }
        }
    }

    fun createFolder(name: String) {
        val newFolder = File(_currentDir.value, name)
        if (newFolder.exists()) {
            showMessage("Directory already exists")
            return
        }
        if (newFolder.mkdirs()) {
            showMessage("Created folder '$name'")
            loadDirectory(_currentDir.value)
            recomputeStorageStats()
        } else {
            showMessage("Failed to create folder")
        }
    }

    fun renameFile(path: String, newName: String) {
        val src = File(path)
        val dst = File(src.parentFile, newName)
        if (dst.exists()) {
            showMessage("A file or folder already exists with this name")
            return
        }
        if (src.renameTo(dst)) {
            showMessage("Renamed to '$newName'")
            loadDirectory(_currentDir.value)
        } else {
            showMessage("Rename failed")
        }
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            val file = File(path)
            repository.moveToRecycleBin(file)
            showMessage("Moved '${file.name}' to Recycle Bin")
            loadDirectory(_currentDir.value)
            recomputeStorageStats()
        }
    }

    fun deletePermanently(path: String) {
        viewModelScope.launch {
            val file = File(path)
            ZenFileManager.deleteRecursively(file)
            showMessage("Permanently deleted '${file.name}'")
            loadDirectory(_currentDir.value)
            recomputeStorageStats()
        }
    }

    fun restoreRecycledItem(entity: FileMetaEntity) {
        viewModelScope.launch {
            repository.restoreFromRecycleBin(entity)
            showMessage("Restored '${entity.name}' successfully")
            loadDirectory(_currentDir.value)
            recomputeStorageStats()
        }
    }

    fun deleteRecycledPermanently(entity: FileMetaEntity) {
        viewModelScope.launch {
            repository.deletePermanentlyRecycled(entity)
            showMessage("Deleted '${entity.name}' permanently")
        }
    }

    fun clearRecycleBin() {
        viewModelScope.launch {
            repository.clearRecycleBin()
            showMessage("Recycle Bin Cleared!")
        }
    }

    fun moveToSecureVault(fileItem: ZenFileManager.FileItem) {
        viewModelScope.launch {
            val file = File(fileItem.path)
            repository.addToVault(file)
            showMessage("Moved '${file.name}' into Hidden Secure Vault")
            loadDirectory(_currentDir.value)
            recomputeStorageStats()
        }
    }

    fun restoreFromVault(entity: FileMetaEntity) {
        viewModelScope.launch {
            repository.restoreFromVault(entity)
            showMessage("Restored '${entity.name}' from Secure Vault")
            loadDirectory(_currentDir.value)
            recomputeStorageStats()
        }
    }

    fun deleteFromVaultPermanently(entity: FileMetaEntity) {
        viewModelScope.launch {
            repository.deleteFromVaultPermanently(entity)
            showMessage("Permanently erased '${entity.name}' from Secure Vault")
            recomputeStorageStats()
        }
    }

    /**
     * Multi selection clipboard actions
     */
    fun toggleSelection(path: String) {
        val updated = _selectedPaths.value.toMutableSet()
        if (updated.contains(path)) {
            updated.remove(path)
        } else {
            updated.add(path)
        }
        _selectedPaths.value = updated
        _multiSelectMode.value = updated.isNotEmpty()
    }

    fun clearSelection() {
        _selectedPaths.value = emptySet()
        _multiSelectMode.value = false
    }

    fun setClipboard(action: String) { // "copy" or "move"
        _clipboardAction.value = action
        _clipboardPaths.value = _selectedPaths.value
        showMessage("Selected items copied to clipboard")
        clearSelection()
    }

    fun pasteClipboard() {
        val action = _clipboardAction.value ?: return
        val paths = _clipboardPaths.value
        if (paths.isEmpty()) return

        showMessage("Processing paste action...")
        viewModelScope.launch {
            for (p in paths) {
                val srcFile = File(p)
                if (srcFile.exists()) {
                    val destFile = File(_currentDir.value, srcFile.name)
                    if (action == "copy") {
                        ZenFileManager.copyRecursively(srcFile, destFile)
                    } else if (action == "move") {
                        ZenFileManager.copyRecursively(srcFile, destFile)
                        ZenFileManager.deleteRecursively(srcFile)
                    }
                }
            }
            _clipboardAction.value = null
            _clipboardPaths.value = emptySet()
            showMessage("Operation completed successfully")
            loadDirectory(_currentDir.value)
            recomputeStorageStats()
        }
    }

    fun archiveSelectedToZip(zipName: String) {
        val paths = _selectedPaths.value
        if (paths.isEmpty()) return
        viewModelScope.launch {
            val outZipFile = File(_currentDir.value, if (zipName.endsWith(".zip")) zipName else "$zipName.zip")
            val sourceFiles = paths.map { File(it) }
            val ok = ZenFileManager.zipFiles(sourceFiles, outZipFile)
            if (ok) {
                showMessage("Zipped selected files to ${outZipFile.name}")
                loadDirectory(_currentDir.value)
                recomputeStorageStats()
            } else {
                showMessage("Zip archive creation failed")
            }
            clearSelection()
        }
    }

    fun extractZipFile(fileItem: ZenFileManager.FileItem) {
        viewModelScope.launch {
            val zipFile = File(fileItem.path)
            val baseName = zipFile.nameWithoutExtension
            val destDir = File(_currentDir.value, baseName + "_extracted")
            val ok = ZenFileManager.unzip(zipFile, destDir)
            if (ok) {
                showMessage("Extraction successful in folder '${destDir.name}'")
                loadDirectory(_currentDir.value)
                recomputeStorageStats()
            } else {
                showMessage("Extraction failed")
            }
        }
    }

    /**
     * Storage scanner
     */
    fun recomputeStorageStats() {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val root = ZenFileManager.getActiveStorageRoot(app)
            val isSandbox = ZenFileManager.getStorageType() == ZenFileManager.STORAGE_SANDBOX

            val total = if (isSandbox) 150 * 1024 * 1024L else root.totalSpace
            var free = if (isSandbox) 78 * 1024 * 1024L else root.freeSpace

            var docs = 0L
            var imgs = 0L
            var audio = 0L
            var vids = 0L
            var apks = 0L
            var zips = 0L
            var other = 0L

            ZenFileManager.scanStatisticsRecurse(root) { file, category ->
                val length = file.length()
                when (category) {
                    "Documents" -> docs += length
                    "Images" -> imgs += length
                    "Audio" -> audio += length
                    "Videos" -> vids += length
                    "APKs" -> apks += length
                    "Archives" -> zips += length
                    else -> other += length
                }
            }

            if (isSandbox) {
                val totalUsed = docs + imgs + audio + vids + apks + zips + other
                free = total - totalUsed
            }

            _storageStats.value = StorageStats(
                totalSpace = total,
                freeSpace = free,
                docsSize = docs,
                imgsSize = imgs,
                audioSize = audio,
                vidsSize = vids,
                apksSize = apks,
                archivesSize = zips,
                othersSize = other
            )
        }
    }

    fun scanDuplicates() {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val root = ZenFileManager.getActiveStorageRoot(app)
            val allFiles = mutableListOf<File>()
            ZenFileManager.scanStatisticsRecurse(root) { f, _ ->
                allFiles.add(f)
            }

            // Find files with exact same length > 0
            val sizeMap = allFiles.groupBy { it.length() }.filter { it.value.size > 1 && it.key > 0 }
            val groups = mutableListOf<DuplicateGroup>()
            sizeMap.forEach { (size, filesList) ->
                // Sub-group by name or just treat them as visual duplicates for this explorer demo
                groups.add(DuplicateGroup(
                    name = filesList.first().name,
                    size = size,
                    instances = filesList
                ))
            }
            _duplicateGroups.value = groups
        }
    }

    /**
     * AI Assistant Functions
     */
    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(prompt, true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiGenerating.value = true

        viewModelScope.launch {
            val response = repository.askAiAssistant(prompt)
            _chatMessages.value = _chatMessages.value + ChatMessage(response, false)
            _isAiGenerating.value = false
        }
    }

    fun requestFolderSummary(folderPath: String) {
        _isAiGenerating.value = true
        viewModelScope.launch {
            val resp = repository.generateFolderSummary(folderPath)
            _chatMessages.value = _chatMessages.value + ChatMessage("AI folder summary generated for: ${File(folderPath).name}", true)
            _chatMessages.value = _chatMessages.value + ChatMessage(resp, false)
            _isAiGenerating.value = false
            _currentScreen.value = "ai"
        }
    }

    fun runSemanticSearch(query: String) {
        if (query.isBlank()) return
        _isAiGenerating.value = true
        _chatMessages.value = _chatMessages.value + ChatMessage("Semantic search: \"$query\"", true)

        viewModelScope.launch {
            val matchedPaths = repository.aiFileSearch(query)
            if (matchedPaths.isEmpty()) {
                _chatMessages.value = _chatMessages.value + ChatMessage("No files matching \"$query\" found in workspace indexes.", false)
            } else {
                val sb = StringBuilder()
                sb.append("Aesthetic index complete. I discovered the following matches:\n\n")
                matchedPaths.forEach { p ->
                    val file = File(p)
                    sb.append("📂 **${file.name}**\n")
                    sb.append("`$p` (${file.length()} bytes)\n\n")
                }
                _chatMessages.value = _chatMessages.value + ChatMessage(sb.toString(), false)
            }
            _isAiGenerating.value = false
        }
    }

    fun performAiSearch(query: String) {
        if (query.isBlank()) {
            _aiSearchResults.value = null
            return
        }
        _isAiSearching.value = true
        _aiSearchResults.value = null
        viewModelScope.launch {
            try {
                val matchedPaths = repository.aiFileSearch(query)
                val items = matchedPaths.mapNotNull { path ->
                    val file = File(path)
                    if (file.exists()) {
                        ZenFileManager.FileItem(
                            name = file.name,
                            path = file.absolutePath,
                            isDirectory = file.isDirectory,
                            size = if (file.isDirectory) ZenFileManager.getFolderSize(file) else file.length(),
                            lastModified = file.lastModified(),
                            extension = file.extension,
                            mimeType = ZenFileManager.getMimeType(file),
                            isFavorite = repository.isFavorite(file.absolutePath)
                        )
                    } else null
                }
                _aiSearchResults.value = items
            } catch (e: Exception) {
                Log.e(TAG, "Error performing AI search", e)
                showMessage("AI query failed: ${e.message}")
                _aiSearchResults.value = emptyList()
            } finally {
                _isAiSearching.value = false
            }
        }
    }

    fun clearAiSearch() {
        _aiSearchResults.value = null
    }
}
