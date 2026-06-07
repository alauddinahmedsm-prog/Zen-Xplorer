package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.api.GeminiClient
import com.example.data.database.FileMetaDao
import com.example.data.database.FileMetaEntity
import com.example.data.file.ZenFileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository(
    private val context: Context,
    private val dao: FileMetaDao
) {
    private val TAG = "FileRepository"

    // Database state flows
    val favoriteFiles: Flow<List<FileMetaEntity>> = dao.getItemsByType("FAVORITE").flowOn(Dispatchers.IO)
    val recycleBinFiles: Flow<List<FileMetaEntity>> = dao.getItemsByType("RECYCLE_BIN").flowOn(Dispatchers.IO)
    val vaultFiles: Flow<List<FileMetaEntity>> = dao.getItemsByType("VAULT").flowOn(Dispatchers.IO)
    val pinnedFolders: Flow<List<FileMetaEntity>> = dao.getItemsByType("PINNED").flowOn(Dispatchers.IO)

    suspend fun getFavoritesSync(): List<FileMetaEntity> = withContext(Dispatchers.IO) {
        dao.getItemsByTypeSync("FAVORITE")
    }

    suspend fun isFavorite(path: String): Boolean = withContext(Dispatchers.IO) {
        dao.getItemByPath(path, "FAVORITE") != null
    }

    suspend fun isPinned(path: String): Boolean = withContext(Dispatchers.IO) {
        dao.getItemByPath(path, "PINNED") != null
    }

    suspend fun addPinnedFolder(file: File) = withContext(Dispatchers.IO) {
        val existing = dao.getItemsByTypeSync("PINNED")
        if (existing.any { it.originalPath == file.absolutePath }) return@withContext
        val nextIndex = existing.size
        val entity = FileMetaEntity(
            originalPath = file.absolutePath,
            currentPath = file.absolutePath,
            name = file.name,
            size = if (file.isDirectory) ZenFileManager.getFolderSize(file) else file.length(),
            isFolder = true,
            mimeType = "directory",
            dateModified = file.lastModified(),
            fileType = "PINNED",
            extraData = nextIndex.toString()
        )
        dao.insertItem(entity)
    }

    suspend fun removePinnedFolder(path: String) = withContext(Dispatchers.IO) {
        dao.deleteItem(path, "PINNED")
        // Re-number indices to keep sequential order in extraData
        val existing = dao.getItemsByTypeSync("PINNED").toMutableList()
        existing.sortBy { it.extraData.toIntOrNull() ?: it.id.toInt() }
        existing.forEachIndexed { i, entity ->
            dao.insertItem(entity.copy(extraData = i.toString()))
        }
    }

    suspend fun reorderPinnedFolder(fromIndex: Int, toIndex: Int) = withContext(Dispatchers.IO) {
        val existing = dao.getItemsByTypeSync("PINNED").toMutableList()
        existing.sortBy { it.extraData.toIntOrNull() ?: it.id.toInt() }
        if (fromIndex in existing.indices && toIndex in existing.indices) {
            val item = existing.removeAt(fromIndex)
            existing.add(toIndex, item)
            existing.forEachIndexed { index, entity ->
                dao.insertItem(entity.copy(extraData = index.toString()))
            }
        }
    }

    suspend fun addFavorite(file: File) = withContext(Dispatchers.IO) {
        val mime = ZenFileManager.getMimeType(file)
        val entity = FileMetaEntity(
            originalPath = file.absolutePath,
            currentPath = file.absolutePath,
            name = file.name,
            size = if (file.isDirectory) ZenFileManager.getFolderSize(file) else file.length(),
            isFolder = file.isDirectory,
            mimeType = mime,
            dateModified = file.lastModified(),
            fileType = "FAVORITE"
        )
        dao.insertItem(entity)
    }

    suspend fun removeFavorite(path: String) = withContext(Dispatchers.IO) {
        dao.deleteItem(path, "FAVORITE")
    }

    /**
     * Move target file into Recycle Bin.
     * We relocate the file physically to app sandbox .RecycleBin/ to protect it,
     * and log the database entry.
     */
    suspend fun moveToRecycleBin(file: File) = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext
        val recycleRoot = ZenFileManager.getRecycleBinDir(context)
        val garbageName = "garbage_at_" + System.currentTimeMillis() + "_" + file.name
        val destFile = File(recycleRoot, garbageName)

        try {
            ZenFileManager.copyRecursively(file, destFile)
            ZenFileManager.deleteRecursively(file)

            val meta = FileMetaEntity(
                originalPath = file.absolutePath,
                currentPath = destFile.absolutePath,
                name = file.name,
                size = if (file.isDirectory) ZenFileManager.getFolderSize(destFile) else destFile.length(),
                isFolder = file.isDirectory,
                mimeType = ZenFileManager.getMimeType(destFile),
                dateModified = System.currentTimeMillis(),
                fileType = "RECYCLE_BIN"
            )
            dao.insertItem(meta)
            Log.d(TAG, "Successfully moved to local database recycle bin: ${file.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed recycling file: ${file.name}", e)
        }
    }

    /**
     * Restore file to its original location
     */
    suspend fun restoreFromRecycleBin(entity: FileMetaEntity) = withContext(Dispatchers.IO) {
        val trashedFile = File(entity.currentPath)
        if (!trashedFile.exists()) {
            dao.deleteItemById(entity.id)
            return@withContext
        }
        val origFile = File(entity.originalPath)
        origFile.parentFile?.mkdirs()

        try {
            ZenFileManager.copyRecursively(trashedFile, origFile)
            ZenFileManager.deleteRecursively(trashedFile)
            dao.deleteItemById(entity.id)
            Log.d(TAG, "Successfully restored recycled entity: ${entity.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed restoring recycled entity: ${entity.name}", e)
        }
    }

    /**
     * Delete recycled file permanently
     */
    suspend fun deletePermanentlyRecycled(entity: FileMetaEntity) = withContext(Dispatchers.IO) {
        val trashedFile = File(entity.currentPath)
        if (trashedFile.exists()) {
            ZenFileManager.deleteRecursively(trashedFile)
        }
        dao.deleteItemById(entity.id)
    }

    suspend fun clearRecycleBin() = withContext(Dispatchers.IO) {
        val recycleBinItems = dao.getItemsByTypeSync("RECYCLE_BIN")
        for (item in recycleBinItems) {
            deletePermanentlyRecycled(item)
        }
    }

    /**
     * Hidden Secure Vault Operations
     */
    suspend fun addToVault(file: File) = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext
        val vaultDir = ZenFileManager.getVaultDir(context)
        val schemaName = "vault_secured_" + System.currentTimeMillis() + "_" + file.name
        val destFile = File(vaultDir, schemaName)

        try {
            // Cut action: copy then delete original
            ZenFileManager.copyRecursively(file, destFile)
            ZenFileManager.deleteRecursively(file)

            val meta = FileMetaEntity(
                originalPath = file.absolutePath,
                currentPath = destFile.absolutePath,
                name = file.name,
                size = if (file.isDirectory) ZenFileManager.getFolderSize(destFile) else destFile.length(),
                isFolder = file.isDirectory,
                mimeType = ZenFileManager.getMimeType(destFile),
                dateModified = System.currentTimeMillis(),
                fileType = "VAULT"
            )
            dao.insertItem(meta)
        } catch (e: Exception) {
            Log.e(TAG, "Failed encrypting file to private vault", e)
        }
    }

    suspend fun restoreFromVault(entity: FileMetaEntity) = withContext(Dispatchers.IO) {
        val vaultFile = File(entity.currentPath)
        if (!vaultFile.exists()) {
            dao.deleteItemById(entity.id)
            return@withContext
        }
        val origFile = File(entity.originalPath)
        origFile.parentFile?.mkdirs()

        try {
            ZenFileManager.copyRecursively(vaultFile, origFile)
            ZenFileManager.deleteRecursively(vaultFile)
            dao.deleteItemById(entity.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed restoring file from secure vault", e)
        }
    }

    suspend fun deleteFromVaultPermanently(entity: FileMetaEntity) = withContext(Dispatchers.IO) {
        val vaultFile = File(entity.currentPath)
        if (vaultFile.exists()) {
            ZenFileManager.deleteRecursively(vaultFile)
        }
        dao.deleteItemById(entity.id)
    }

    /**
     * Ask Gemini AI chat queries
     */
    suspend fun askAiAssistant(prompt: String): String = withContext(Dispatchers.IO) {
        val activeRoot = ZenFileManager.getActiveStorageRoot(context)
        val fileListing = getSimpleFileTreeText(activeRoot, maxDepth = 2)

        val systemInstruction = """
            You are the high-performance AI Core of Zen Xplorer—a premium, dark navy-themed, secure material Android workspace.
            Your task is to assist the user in managing, optimization, scanning, and auditing their files.
            You have access to a snapshot of the workspace storage:
            ---
            $fileListing
            ---
            Be concise, professional, and friendly. Guide user actions on duplicate removals, cleaning recommendations, or general queries. Always suggest directory solutions.
            User's target email: alauddin1991@gmail.com
        """.trimIndent()

        GeminiClient.generateContent(prompt, systemInstruction)
    }

    /**
     * Natural Language semantic search using Gemini.
     * We send a snapshot tree, list of paths, and search query.
     * The model is commanded to return matching full paths in raw JSON array.
     */
    suspend fun aiFileSearch(query: String): List<String> = withContext(Dispatchers.IO) {
        val activeRoot = ZenFileManager.getActiveStorageRoot(context)
        val filesList = mutableListOf<File>()
        ZenFileManager.scanStatisticsRecurse(activeRoot) { file, _ ->
            filesList.add(file)
        }

        if (filesList.isEmpty()) return@withContext emptyList()

        val jsonInputList = filesList.map { f ->
            val relPath = f.absolutePath.replace(activeRoot.absolutePath, "")
            "{\"name\":\"${f.name}\", \"relPath\":\"$relPath\", \"path\":\"${f.absolutePath}\", \"size\":${f.length()}, \"ext\":\"${f.extension}\"}"
        }.joinToString(",\n")

        val systemInstruction = """
            You are the indexing oracle of Zen Xplorer. Given a list of files on the device in JSON, return ONLY a valid raw JSON array containing the matching string absolute file "path" values according to the natural language search query.
            Do not explain why you chose them. Return empty bracket [] if no files match.
            Query: "$query"
        """.trimIndent()

        val fullInput = "[\n$jsonInputList\n]"
        val response = GeminiClient.generateContent(fullInput, systemInstruction)

        try {
            // Locate JSON elements inside response
            val trimmedResponse = response.trim()
            val jsonStartIndex = trimmedResponse.indexOf("[")
            val jsonEndIndex = trimmedResponse.lastIndexOf("]")
            if (jsonStartIndex >= 0 && jsonEndIndex > jsonStartIndex) {
                val jsonPart = trimmedResponse.substring(jsonStartIndex, jsonEndIndex + 1)
                val arr = org.json.JSONArray(jsonPart)
                val matches = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    matches.add(arr.getString(i))
                }
                return@withContext matches
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing semantic search output", e)
        }
        
        // Basic fallback substring matching if model/internet fails
        return@withContext filesList.filter { f ->
            f.name.contains(query, ignoreCase = true) || f.extension.contains(query, ignoreCase = true)
        }.map { it.absolutePath }
    }

    /**
     * Gather summaries for a folder's content
     */
    suspend fun generateFolderSummary(folderPath: String): String = withContext(Dispatchers.IO) {
        val folder = File(folderPath)
        if (!folder.exists() || !folder.isDirectory) {
            return@withContext "Target folder path ${folder.name} is unavailable."
        }
        val items = folder.listFiles() ?: emptyArray()
        val builder = StringBuilder()
        builder.append("Folder: ${folder.name}\n")
        builder.append("Total Items: ${items.size}\n")
        items.take(30).forEach { item ->
            builder.append("- ${item.name} (${if (item.isDirectory) "Dir" else "${item.length()} bytes"})\n")
        }

        val prompt = "Analyze and summarize the contents of this folder concisely, and suggest next steps or organization hints:\n\n${builder}"
        GeminiClient.generateContent(prompt, "You are a senior system layout administrator.")
    }

    private fun getSimpleFileTreeText(root: File, maxDepth: Int): String {
        val sb = java.lang.StringBuilder()
        fun walk(file: File, depth: Int) {
            if (depth > maxDepth) return
            val prefix = "  ".repeat(depth)
            if (file.isDirectory) {
                sb.append("$prefix[D] ${file.name}\n")
                val children = file.listFiles() ?: return
                for (child in children) {
                    if (child.name != ".RecycleBin" && child.name != ".ZenVault") {
                        walk(child, depth + 1)
                    }
                }
            } else {
                sb.append("$prefix[F] ${file.name} (${file.length()} B)\n")
            }
        }
        walk(root, 0)
        return sb.toString()
    }
}
