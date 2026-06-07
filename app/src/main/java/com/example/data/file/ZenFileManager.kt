package com.example.data.file

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZenFileManager {
    private const val TAG = "ZenFileManager"

    // Storage Types
    const val STORAGE_SANDBOX = "SANDBOX"
    const val STORAGE_DEVICE = "DEVICE"

    private var activeStorageType = STORAGE_SANDBOX

    /**
     * Get the sandbox storage directory
     */
    fun getSandboxRoot(context: Context): File {
        val root = File(context.filesDir, "ZenStorage")
        if (!root.exists()) {
            root.mkdirs()
        }
        return root
    }

    /**
     * Get the recycle bin directory
     */
    fun getRecycleBinDir(context: Context): File {
        val dir = File(context.filesDir, ".RecycleBin")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Get the secure hidden vault directory
     */
    fun getVaultDir(context: Context): File {
        val dir = File(context.filesDir, ".ZenVault")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getActiveStorageRoot(context: Context): File {
        return if (activeStorageType == STORAGE_SANDBOX) {
            getSandboxRoot(context)
        } else {
            Environment.getExternalStorageDirectory() ?: getSandboxRoot(context)
        }
    }

    fun setStorageType(type: String) {
        activeStorageType = type
    }

    fun getStorageType(): String = activeStorageType

    /**
     * Initialize sample files and directories in Sandbox on first-ever launch
     */
    fun initializeSandboxIfNeeded(context: Context) {
        val sandboxRoot = getSandboxRoot(context)
        val initializedMarker = File(context.filesDir, ".sandbox_initialized")
        if (initializedMarker.exists() && sandboxRoot.listFiles()?.isNotEmpty() == true) {
            return
        }

        try {
            // Create directories
            val docsDir = File(sandboxRoot, "Documents")
            val imgsDir = File(sandboxRoot, "Images")
            val audioDir = File(sandboxRoot, "Audio")
            val vidsDir = File(sandboxRoot, "Videos")
            val apksDir = File(sandboxRoot, "APKs")
            val dlsDir = File(sandboxRoot, "Downloads")
            val archDir = File(sandboxRoot, "Archives")

            val dirs = listOf(docsDir, imgsDir, audioDir, vidsDir, apksDir, dlsDir, archDir)
            for (dir in dirs) {
                if (!dir.exists()) dir.mkdirs()
            }

            // Create sample documents
            createSampleTextFile(docsDir, "Invoice_2026_May.txt", "Zen Xplorer App Inc.\nDate: May 15, 2026\nInvoice ID: #ZX-2026-0515\nAmount Due: $1,450.00\nItems:\n- 1x Android Cloud Server Setup ($950.00)\n- 5x Enterprise Premium Cloud Licenses ($500.00)\nStatus: Paid")
            createSampleTextFile(docsDir, "Project_Milestones_June.txt", "Zen Xplorer Development Timeline:\n- Milestone 1: Jetpack Compose UI Prototypes (Complete)\n- Milestone 2: Room Database & Local File Manager Utilities (Complete)\n- Milestone 3: AI Assistant & Natural Language Search (Complete)\n- Milestone 4: Final Polishing & APK Compilation Release (June 28, 2026)")
            createSampleTextFile(docsDir, "ZenExplorer_Whitepaper.txt", "Zen Xplorer (ZX) is a state-of-the-art offline-first, private-focused Android workspace application. It incorporates advanced AI intelligence to analyze structural disk footprint distributions and locate files naturally through deep NLP integration using Gemini APIs.")

            // Create contextual item for Alaudin
            createSampleTextFile(dlsDir, "Resume_Alauddin_2026.txt", "Alauddin Ahmed\nEmail: alauddin1991@gmail.com\nSpecialty: Lead System Architect, Android & Cloud Infrastructure Specialist\nExpertise:\n- Kotlin, Java, Jetpack Compose, Coroutines\n- Google Cloud, Spanner, Firestore, Firebase AI\n- Advanced security protocols and localized encryption algorithms\nStatus: Active Project Search")
            createSampleTextFile(dlsDir, "Receipt_Booking_3012.txt", "Zen Airline Booker\nPassenger: Alauddin Ahmed\nFlight ID: AB-3012-DEL\nDeparture: Singapore (SIN)\nArrival: Dhaka (DAC)\nClass: Premium Executive Studio\nStatus: Confirmed")

            // Create sample image placeholders (plain text descriptors to represent imagery data)
            createSampleTextFile(imgsDir, "Mountain_Zen.jpg", "[BINARY_IMAGE_DATA_ZEN_MOUNTAIN_SUNSET_OVER_BLUE_HILLSCAPE]")
            createSampleTextFile(imgsDir, "Product_Wireframe.png", "[BINARY_IMAGE_DATA_PRODUCT_WIREFRAME_MOBILE_EXPLORER_M3]")
            createSampleTextFile(imgsDir, "Avatar_Default.png", "[BINARY_IMAGE_DATA_USER_AVATAR_CIRCLE_TRANSPARENT_GLASSY]")

            // Create sample Audios
            createSampleTextFile(audioDir, "Deep_Meditation_Relax.mp3", "[BINARY_AUDIO_DATA_OCEAN_STREAMS_SINE_WAVE_MINUTES_25]")
            createSampleTextFile(audioDir, "Zen_Nature_Stream.wav", "[BINARY_AUDIO_DATA_FOREST_RAIN_AND_BIRDS_WAVE_96KHZ]")

            // Create sample Videos
            createSampleTextFile(vidsDir, "Sleek_App_Intro.mp4", "[BINARY_VIDEO_DATA_AESTHETIC_INTENTIONAL_BACKGROUNDS]")

            // Create sample APK placeholders
            createSampleTextFile(apksDir, "zen_explorer_v1_0.apk", "[ANDROID_APPLICATION_PACKAGE_COM_AISTUDIO_ZENXPLORER]")
            createSampleTextFile(apksDir, "smart_optimizer_alpha.apk", "[ANDROID_APPLICATION_PACKAGE_COM_AISTUDIO_OPTIMIZER]")

            // Create sample archives
            createSampleTextFile(archDir, "Backup_Photos_2025.zip", "[ZIP_ARCHIVE_CONTAINING_VACATION_PHOTOS_REPORTS]")

            // Generate a duplicate file recommendation demonstration
            createSampleTextFile(dlsDir, "Invoice_2026_May_Duplicate.txt", "Zen Xplorer App Inc.\nDate: May 15, 2026\nInvoice ID: #ZX-2026-0515\nAmount Due: $1,450.00\nItems:\n- 1x Android Cloud Server Setup ($950.00)\n- 5x Enterprise Premium Cloud Licenses ($500.00)\nStatus: Paid")

            initializedMarker.writeText("INITIALIZED")
            Log.d(TAG, "Successfully initialized mock sandbox sandbox storage files.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed initializing mockup directories", e)
        }
    }

    private fun createSampleTextFile(parent: File, name: String, content: String) {
        val file = File(parent, name)
        file.writeText(content)
    }

    /**
     * File details data wrapper
     */
    data class FileItem(
        val name: String,
        val path: String,
        val isDirectory: Boolean,
        val size: Long,
        val lastModified: Long,
        val extension: String,
        val mimeType: String,
        val isFavorite: Boolean = false,
        val isBookmarked: Boolean = false
    )

    /**
     * List all files in a given directory path
     */
    fun listFilesInDirectory(directory: File): List<FileItem> {
        val fileItems = mutableListOf<FileItem>()
        val files = directory.listFiles() ?: return emptyList()
        for (f in files) {
            val extension = f.extension
            val isDir = f.isDirectory
            val size = if (isDir) getFolderSize(f) else f.length()
            val mime = getMimeType(f)
            fileItems.add(
                FileItem(
                    name = f.name,
                    path = f.absolutePath,
                    isDirectory = isDir,
                    size = size,
                    lastModified = f.lastModified(),
                    extension = extension,
                    mimeType = mime
                )
            )
        }
        // Directories first, then alphabetical list
        return fileItems.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
    }

    /**
     * Calculate size of file or recursive folder
     */
    fun getFolderSize(file: File): Long {
        if (!file.exists()) return 0
        if (!file.isDirectory) return file.length()
        var size: Long = 0
        val files = file.listFiles() ?: return 0
        for (f in files) {
            size += if (f.isDirectory) getFolderSize(f) else f.length()
        }
        return size
    }

    /**
     * Clean system mime types or fallback on generic categories
     */
    fun getMimeType(file: File): String {
        if (file.isDirectory) return "folder"
        val ext = file.extension.lowercase()
        return when (ext) {
            "jpg", "jpeg", "png", "webp", "gif", "bmp" -> "image/${ext}"
            "mp3", "wav", "ogg", "aac", "m4a", "flac" -> "audio/${ext}"
            "mp4", "mkv", "avi", "mov", "webm", "3gp" -> "video/${ext}"
            "pdf" -> "application/pdf"
            "txt", "log", "json", "xml", "csv", "kt", "java" -> "text/plain"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            "zip", "rar", "7z", "tar", "gz" -> "application/zip"
            "apk" -> "application/vnd.android.package-archive"
            else -> "application/octet-stream"
        }
    }

    fun getCategoryName(file: File): String {
        if (file.isDirectory) return "Folder"
        val ext = file.extension.lowercase()
        return when (ext) {
            "jpg", "jpeg", "png", "webp", "gif" -> "Images"
            "mp4", "mkv", "avi", "mov", "webm" -> "Videos"
            "mp3", "wav", "ogg", "m4a" -> "Audio"
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" -> "Documents"
            "apk" -> "APKs"
            "zip", "7z", "rar", "tar" -> "Archives"
            else -> "Other"
        }
    }

    /**
     * Delete utility
     */
    fun deleteRecursively(file: File): Boolean {
        if (file.isDirectory) {
            val children = file.listFiles()
            if (children != null) {
                for (child in children) {
                    deleteRecursively(child)
                }
            }
        }
        return file.delete()
    }

    /**
     * Copy utility
     */
    fun copyRecursively(sourceFile: File, destFile: File) {
        if (sourceFile.isDirectory) {
            if (!destFile.exists()) {
                destFile.mkdirs()
            }
            val children = sourceFile.list() ?: return
            for (child in children) {
                copyRecursively(File(sourceFile, child), File(destFile, child))
            }
        } else {
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8192)
                    var length: Int
                    while (input.read(buffer).also { length = it } > 0) {
                        output.write(buffer, 0, length)
                    }
                }
            }
        }
    }

    /**
     * Compress files or directory to a ZIP archive
     */
    fun zipFiles(sourceFiles: List<File>, zipOutFile: File): Boolean {
        return try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipOutFile))).use { zos ->
                for (sourceFile in sourceFiles) {
                    addFileToZip(zos, sourceFile, sourceFile.name)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed creating ZIP compression file", e)
            false
        }
    }

    private fun addFileToZip(zos: ZipOutputStream, fileToZip: File, parentDirectoryName: String) {
        if (fileToZip.isDirectory) {
            val children = fileToZip.listFiles() ?: return
            for (childFile in children) {
                addFileToZip(zos, childFile, parentDirectoryName + "/" + childFile.name)
            }
        } else {
            val buffer = ByteArray(8192)
            FileInputStream(fileToZip).use { fis ->
                val zipEntry = ZipEntry(parentDirectoryName)
                zos.putNextEntry(zipEntry)
                var length: Int
                while (fis.read(buffer).also { length = it } > 0) {
                    zos.write(buffer, 0, length)
                }
                zos.closeEntry()
            }
        }
    }

    /**
     * Unzip/Extract standard file
     */
    fun unzip(zipFile: File, targetDir: File): Boolean {
        return try {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    val filePath = File(targetDir, entry.name)
                    if (!entry.isDirectory) {
                        // Check parent folder
                        filePath.parentFile?.mkdirs()
                        FileOutputStream(filePath).use { fos ->
                            val buffer = ByteArray(8192)
                            var len: Int
                            while (zipIn.read(buffer).also { len = it } > 0) {
                                fos.write(buffer, 0, len)
                            }
                        }
                    } else {
                        filePath.mkdirs()
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract ZIP archive", e)
            false
        }
    }

    /**
     * Scan storage recursively to get category statistics, large files, and duplicates
     */
    fun scanStatisticsRecurse(root: File, callback: (File, String) -> Unit) {
        val files = root.listFiles() ?: return
        for (f in files) {
            if (f.isDirectory) {
                if (f.name != ".RecycleBin" && f.name != ".ZenVault") {
                    scanStatisticsRecurse(f, callback)
                }
            } else {
                val category = getCategoryName(f)
                callback(f, category)
            }
        }
    }
}
