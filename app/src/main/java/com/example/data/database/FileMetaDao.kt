package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FileMetaDao {
    @Query("SELECT * FROM zen_file_meta WHERE fileType = :type ORDER BY id DESC")
    fun getItemsByType(type: String): Flow<List<FileMetaEntity>>

    @Query("SELECT * FROM zen_file_meta WHERE fileType = :type")
    suspend fun getItemsByTypeSync(type: String): List<FileMetaEntity>

    @Query("SELECT * FROM zen_file_meta WHERE originalPath = :path AND fileType = :type LIMIT 1")
    suspend fun getItemByPath(path: String, type: String): FileMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(entity: FileMetaEntity)

    @Query("DELETE FROM zen_file_meta WHERE originalPath = :path AND fileType = :type")
    suspend fun deleteItem(path: String, type: String)

    @Query("DELETE FROM zen_file_meta WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM zen_file_meta WHERE fileType = :type")
    suspend fun clearType(type: String)
}
