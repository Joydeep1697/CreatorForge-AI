package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContentItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT * FROM content_items ORDER BY timestamp DESC")
    fun getAllContentItems(): Flow<List<ContentItem>>

    @Query("SELECT * FROM content_items WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteContentItems(): Flow<List<ContentItem>>

    @Query("SELECT * FROM content_items WHERE type = :type ORDER BY timestamp DESC")
    fun getContentItemsByType(type: String): Flow<List<ContentItem>>

    @Query("SELECT * FROM content_items WHERE id = :id")
    suspend fun getContentItemById(id: Long): ContentItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentItem(item: ContentItem): Long

    @Update
    suspend fun updateContentItem(item: ContentItem)

    @Delete
    suspend fun deleteContentItem(item: ContentItem)

    @Query("UPDATE content_items SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFav: Boolean)

    @Query("DELETE FROM content_items")
    suspend fun clearAll()
}
