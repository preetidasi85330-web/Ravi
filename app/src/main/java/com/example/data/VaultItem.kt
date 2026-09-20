package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VaultCategory {
    PHOTO,
    VIDEO,
    DOCUMENT,
    NOTE
}

@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val originalFileName: String,
    val vaultFilePath: String, // internal secure file path
    val category: VaultCategory,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val noteContent: String? = null,
    val isFavorite: Boolean = false
)
