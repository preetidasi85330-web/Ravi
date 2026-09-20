package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.VaultCategory
import com.example.data.VaultItem
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultCardDark
import com.example.ui.theme.VaultCardElevated
import com.example.ui.theme.VaultError
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.VaultSecondary
import com.example.ui.theme.VaultTextPrimary
import com.example.ui.theme.VaultTextSecondary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPreviewDialog(
    item: VaultItem,
    onDismiss: () -> Unit,
    onUnhide: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onUpdateNote: (String, String) -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF0090D16))
        ) {
            when (item.category) {
                VaultCategory.PHOTO -> {
                    PhotoViewer(
                        item = item,
                        onClose = onDismiss,
                        onUnhide = onUnhide,
                        onDelete = onDelete,
                        onShare = onShare
                    )
                }
                VaultCategory.VIDEO -> {
                    VideoViewer(
                        item = item,
                        onClose = onDismiss,
                        onUnhide = onUnhide,
                        onDelete = onDelete,
                        onShare = onShare,
                        onOpenVideo = {
                            val file = File(item.vaultFilePath)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, item.mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Play Video"))
                        }
                    )
                }
                VaultCategory.NOTE -> {
                    NoteViewer(
                        item = item,
                        onClose = onDismiss,
                        onSave = onUpdateNote,
                        onDelete = onDelete,
                        onShare = onShare
                    )
                }
                VaultCategory.DOCUMENT -> {
                    DocumentViewer(
                        item = item,
                        onClose = onDismiss,
                        onUnhide = onUnhide,
                        onDelete = onDelete,
                        onShare = onShare,
                        onOpenDocument = {
                            val file = File(item.vaultFilePath)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, item.mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Open File"))
                            } catch (_: Exception) {
                                // gracefully handled
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoViewer(
    item: VaultItem,
    onClose: () -> Unit,
    onUnhide: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        offset = if (scale > 1f) offset + offsetChange else Offset.Zero
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Zoomable Image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(File(item.vaultFilePath))
                    .build(),
                contentDescription = item.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        }

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .background(Color(0x99000000), CircleShape)
                    .testTag("close_preview_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = VaultTextPrimary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = VaultTextPrimary
                )
                Text(
                    text = formatFileSize(item.sizeBytes),
                    fontSize = 12.sp,
                    color = VaultTextSecondary
                )
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier.background(Color(0x99000000), CircleShape)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = VaultTextPrimary)
            }
        }

        // Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0xCC111827))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onUnhide,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = VaultPrimary.copy(alpha = 0.2f),
                    contentColor = VaultPrimary
                ),
                modifier = Modifier.testTag("unhide_button")
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unhide to Gallery")
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .background(VaultError.copy(alpha = 0.15f), CircleShape)
                    .testTag("delete_vault_item_button")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VaultError)
            }
        }
    }
}

@Composable
private fun VideoViewer(
    item: VaultItem,
    onClose: () -> Unit,
    onUnhide: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onOpenVideo: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.background(Color(0x99000000), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = VaultTextPrimary)
            }
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = VaultTextPrimary
            )
            IconButton(
                onClick = onShare,
                modifier = Modifier.background(Color(0x99000000), CircleShape)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = VaultTextPrimary)
            }
        }

        // Thumbnail Preview with Play Overlay
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = VaultCardDark),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(File(item.vaultFilePath))
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                Button(
                    onClick = onOpenVideo,
                    colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.testTag("play_video_button")
                ) {
                    Icon(
                        Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color(0xFF00363D)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Play Video",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00363D)
                    )
                }
            }
        }

        // Details & Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VaultCardDark, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Size: ${formatFileSize(item.sizeBytes)}",
                    fontSize = 13.sp,
                    color = VaultTextSecondary
                )
                Text(
                    text = "Protected in Vault",
                    fontSize = 12.sp,
                    color = VaultSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = onUnhide,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = VaultPrimary.copy(alpha = 0.2f),
                        contentColor = VaultPrimary
                    )
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unhide")
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.background(VaultError.copy(alpha = 0.15f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VaultError)
                }
            }
        }
    }
}

@Composable
private fun NoteViewer(
    item: VaultItem,
    onClose: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var content by remember { mutableStateOf(item.noteContent ?: "") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 44.dp),
        shape = RoundedCornerShape(24.dp),
        color = VaultCardDark,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = VaultTextPrimary)
                }

                Text(
                    text = "Secret Note",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = VaultPrimary
                )

                Row {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = VaultTextSecondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VaultError)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Note Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VaultPrimary,
                    unfocusedBorderColor = VaultBorder,
                    focusedTextColor = VaultTextPrimary,
                    unfocusedTextColor = VaultTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Content Field
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Confidential Note Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VaultPrimary,
                    unfocusedBorderColor = VaultBorder,
                    focusedTextColor = VaultTextPrimary,
                    unfocusedTextColor = VaultTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = { onSave(title, content) },
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_note_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF00363D))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Changes",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00363D)
                )
            }
        }
    }
}

@Composable
private fun DocumentViewer(
    item: VaultItem,
    onClose: () -> Unit,
    onUnhide: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onOpenDocument: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 60.dp),
        shape = RoundedCornerShape(24.dp),
        color = VaultCardDark,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = VaultTextPrimary)
                }
                Text(
                    text = "Protected File",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = VaultPrimary
                )
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = VaultTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(VaultPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = VaultPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = VaultTextPrimary
            )

            Text(
                text = "Original Name: ${item.originalFileName}",
                fontSize = 12.sp,
                color = VaultTextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = VaultCardElevated),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Size:", fontSize = 12.sp, color = VaultTextSecondary)
                        Text(formatFileSize(item.sizeBytes), fontSize = 12.sp, color = VaultTextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Type:", fontSize = 12.sp, color = VaultTextSecondary)
                        Text(item.mimeType, fontSize = 12.sp, color = VaultTextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status:", fontSize = 12.sp, color = VaultTextSecondary)
                        Text("Quarantined & Encrypted", fontSize = 12.sp, color = VaultSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onOpenDocument,
                colors = ButtonDefaults.buttonColors(containerColor = VaultPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("open_document_button")
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF00363D))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open With App", fontWeight = FontWeight.Bold, color = Color(0xFF00363D))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onUnhide,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = VaultPrimary.copy(alpha = 0.2f),
                        contentColor = VaultPrimary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Unhide")
                }

                FilledTonalButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = VaultError.copy(alpha = 0.2f),
                        contentColor = VaultError
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete")
                }
            }
        }
    }
}
