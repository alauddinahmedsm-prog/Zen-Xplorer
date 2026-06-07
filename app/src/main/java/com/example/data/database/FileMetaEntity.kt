package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zen_file_meta")
data class FileMetaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originalPath: String,
    val currentPath: String,
    val name: String,
    val size: Long,
    val isFolder: Boolean,
    val mimeType: String,
    val dateModified: Long,
    val fileType: String, // "FAVORITE", "RECYCLE_BIN", "VAULT", "BOOKMARK"
    val extraData: String = ""
)
