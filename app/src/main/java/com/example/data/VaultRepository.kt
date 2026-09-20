package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class VaultRepository(
    private val context: Context,
    private val vaultDao: VaultDao
) {
    private val vaultRootDir: File = File(context.filesDir, "vault").apply {
        if (!exists()) mkdirs()
    }

    fun getAllItems(): Flow<List<VaultItem>> = vaultDao.getAllItems()

    fun getItemsByCategory(category: VaultCategory): Flow<List<VaultItem>> =
        vaultDao.getItemsByCategory(category)

    fun searchItems(query: String): Flow<List<VaultItem>> = vaultDao.searchItems(query)

    fun getCountByCategory(category: VaultCategory): Flow<Int> =
        vaultDao.getCountByCategory(category)

    fun getTotalVaultSizeBytes(): Flow<Long?> = vaultDao.getTotalVaultSizeBytes()

    suspend fun importUri(
        uri: Uri,
        forcedCategory: VaultCategory? = null,
        customTitle: String? = null
    ): Result<VaultItem> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var fileName = "vault_file_${System.currentTimeMillis()}"
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        cursor.getString(nameIndex)?.let { fileName = it }
                    }
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            val mimeType = contentResolver.getType(uri) ?: getMimeTypeFromExtension(fileName) ?: "application/octet-stream"

            val category = forcedCategory ?: detectCategory(mimeType, fileName)

            val categoryDir = File(vaultRootDir, category.name.lowercase()).apply {
                if (!exists()) mkdirs()
            }

            val extension = getExtension(fileName)
            val secureFileName = "enc_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.${extension.ifEmpty { "dat" }}"
            val destinationFile = File(categoryDir, secureFileName)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Cannot open file stream"))

            if (fileSize <= 0) {
                fileSize = destinationFile.length()
            }

            val title = customTitle?.takeIf { it.isNotBlank() } ?: fileName

            val vaultItem = VaultItem(
                title = title,
                originalFileName = fileName,
                vaultFilePath = destinationFile.absolutePath,
                category = category,
                mimeType = mimeType,
                sizeBytes = fileSize,
                createdAt = System.currentTimeMillis()
            )

            val id = vaultDao.insertItem(vaultItem)
            Result.success(vaultItem.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSecretNote(title: String, content: String): Long = withContext(Dispatchers.IO) {
        val bytes = content.toByteArray(Charsets.UTF_8)
        val noteDir = File(vaultRootDir, "note").apply { if (!exists()) mkdirs() }
        val noteFile = File(noteDir, "note_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.txt")
        noteFile.writeBytes(bytes)

        val item = VaultItem(
            title = title.ifBlank { "Untitled Secret Note" },
            originalFileName = "${title.ifBlank { "Note" }}.txt",
            vaultFilePath = noteFile.absolutePath,
            category = VaultCategory.NOTE,
            mimeType = "text/plain",
            sizeBytes = bytes.size.toLong(),
            noteContent = content
        )
        vaultDao.insertItem(item)
    }

    suspend fun updateSecretNote(item: VaultItem, newTitle: String, newContent: String) = withContext(Dispatchers.IO) {
        val file = File(item.vaultFilePath)
        val bytes = newContent.toByteArray(Charsets.UTF_8)
        file.writeBytes(bytes)
        val updated = item.copy(
            title = newTitle.ifBlank { "Untitled Secret Note" },
            noteContent = newContent,
            sizeBytes = bytes.size.toLong()
        )
        vaultDao.updateItem(updated)
    }

    suspend fun deleteItem(item: VaultItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(item.vaultFilePath)
            if (file.exists()) {
                file.delete()
            }
            vaultDao.deleteById(item.id)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getShareIntent(item: VaultItem): Intent? {
        val file = File(item.vaultFilePath)
        if (!file.exists()) return null
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)

        return Intent(Intent.ACTION_SEND).apply {
            type = item.mimeType.ifEmpty { "*/*" }
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, item.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    suspend fun unhideToPublic(item: VaultItem): Result<String> = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(item.vaultFilePath)
            if (!srcFile.exists()) {
                return@withContext Result.failure(Exception("File not found in vault"))
            }

            val fileName = item.originalFileName
            val mimeType = item.mimeType

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val collection = when (item.category) {
                    VaultCategory.PHOTO -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    VaultCategory.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
                }

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(collection, values)
                    ?: return@withContext Result.failure(Exception("Failed to create destination uri"))

                context.contentResolver.openOutputStream(uri)?.use { outStream ->
                    FileInputStream(srcFile).use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }

                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)

                Result.success("Restored $fileName to gallery/downloads")
            } else {
                @Suppress("DEPRECATION")
                val publicDir = when (item.category) {
                    VaultCategory.PHOTO -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    VaultCategory.VIDEO -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                    else -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                }
                if (!publicDir.exists()) publicDir.mkdirs()
                val targetFile = File(publicDir, fileName)
                FileInputStream(srcFile).use { inStream ->
                    FileOutputStream(targetFile).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }
                Result.success("Restored to ${targetFile.absolutePath}")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun detectCategory(mimeType: String, fileName: String): VaultCategory {
        val lowerMime = mimeType.lowercase()
        val lowerExt = getExtension(fileName).lowercase()

        return when {
            lowerMime.startsWith("image/") || lowerExt in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "svg") -> VaultCategory.PHOTO
            lowerMime.startsWith("video/") || lowerExt in listOf("mp4", "mkv", "mov", "avi", "webm", "3gp", "flv") -> VaultCategory.VIDEO
            lowerMime.startsWith("text/") || lowerExt in listOf("txt", "log", "md") -> VaultCategory.NOTE
            else -> VaultCategory.DOCUMENT
        }
    }

    private fun getExtension(name: String): String {
        val dot = name.lastIndexOf('.')
        return if (dot != -1 && dot < name.length - 1) name.substring(dot + 1) else ""
    }

    private fun getMimeTypeFromExtension(name: String): String? {
        val ext = getExtension(name)
        if (ext.isEmpty()) return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
    }
}
