package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultDao {

    @Query("SELECT * FROM vault_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE category = :category ORDER BY createdAt DESC")
    fun getItemsByCategory(category: VaultCategory): Flow<List<VaultItem>>

    @Query("SELECT * FROM vault_items WHERE title LIKE '%' || :query || '%' OR originalFileName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchItems(query: String): Flow<List<VaultItem>>

    @Query("SELECT COUNT(*) FROM vault_items WHERE category = :category")
    fun getCountByCategory(category: VaultCategory): Flow<Int>

    @Query("SELECT SUM(sizeBytes) FROM vault_items")
    fun getTotalVaultSizeBytes(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: VaultItem): Long

    @Update
    suspend fun updateItem(item: VaultItem)

    @Delete
    suspend fun deleteItem(item: VaultItem)

    @Query("DELETE FROM vault_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM vault_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: Long): VaultItem?
}
