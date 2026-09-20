package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultCategory
import com.example.data.VaultItem
import com.example.ui.components.CategoryTabs
import com.example.ui.components.VaultDocumentOrNoteCard
import com.example.ui.components.VaultPhotoOrVideoCard
import com.example.ui.theme.VaultBgDark
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultCardDark
import com.example.ui.theme.VaultCardElevated
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.VaultSecondary
import com.example.ui.theme.VaultTertiary
import com.example.ui.theme.VaultTextPrimary
import com.example.ui.theme.VaultTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
    items: List<VaultItem>,
    allItems: List<VaultItem>,
    selectedCategory: VaultCategory?,
    searchQuery: String,
    totalStorageFormatted: String,
    onSelectCategory: (VaultCategory?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onItemClick: (VaultItem) -> Unit,
    onUnhideItem: (VaultItem) -> Unit,
    onDeleteItem: (VaultItem) -> Unit,
    onShareItem: (VaultItem) -> Unit,
    onLockApp: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAddNote: () -> Unit,
    onImportUris: (List<Uri>, VaultCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSearchField by remember { mutableStateOf(false) }
    var showAddOptionsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    // Activity Result Launchers
    // 1. Photos picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportUris(uris, VaultCategory.PHOTO)
        }
    }

    // 2. Videos picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportUris(uris, VaultCategory.VIDEO)
        }
    }

    // 3. Document / SAF file picker
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportUris(uris, VaultCategory.DOCUMENT)
        }
    }

    val photosCount = remember(allItems) { allItems.count { it.category == VaultCategory.PHOTO } }
    val videosCount = remember(allItems) { allItems.count { it.category == VaultCategory.VIDEO } }
    val docsCount = remember(allItems) { allItems.count { it.category == VaultCategory.DOCUMENT } }
    val notesCount = remember(allItems) { allItems.count { it.category == VaultCategory.NOTE } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VaultBgDark,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(VaultPrimary.copy(alpha = 0.15f))
                                .border(1.dp, VaultPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = VaultPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Secret Vault",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = VaultTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            showSearchField = !showSearchField
                            if (!showSearchField) onSearchQueryChange("")
                        },
                        modifier = Modifier.testTag("toggle_search_button")
                    ) {
                        Icon(
                            imageVector = if (showSearchField) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = VaultTextPrimary
                        )
                    }

                    IconButton(
                        onClick = onLockApp,
                        modifier = Modifier.testTag("lock_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Vault",
                            tint = VaultPrimary
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = VaultTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VaultBgDark,
                    titleContentColor = VaultTextPrimary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddOptionsSheet = true },
                containerColor = VaultPrimary,
                contentColor = Color(0xFF00363D),
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier.testTag("fab_add_item")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hide Files", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Row (animated)
            AnimatedVisibility(
                visible = showSearchField,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search hidden files, photos, notes...", color = VaultTextSecondary) },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = VaultTextSecondary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = VaultTextSecondary)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VaultPrimary,
                            unfocusedBorderColor = VaultBorder,
                            focusedTextColor = VaultTextPrimary,
                            unfocusedTextColor = VaultTextPrimary,
                            focusedContainerColor = VaultCardDark,
                            unfocusedContainerColor = VaultCardDark
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_text_input")
                    )
                }
            }

            // Security Overview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VaultCardDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    VaultPrimary.copy(alpha = 0.08f),
                                    VaultSecondary.copy(alpha = 0.04f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(VaultSecondary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ENCRYPTED & PROTECTED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = VaultSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalStorageFormatted Protected",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = VaultTextPrimary
                            )
                            Text(
                                text = "${allItems.size} private items in secure vault",
                                fontSize = 12.sp,
                                color = VaultTextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(VaultPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = VaultPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Category Filter Pills
            CategoryTabs(
                selectedCategory = selectedCategory,
                onSelectCategory = onSelectCategory,
                photosCount = photosCount,
                videosCount = videosCount,
                docsCount = docsCount,
                notesCount = notesCount
            )

            // Content List / Grid
            if (items.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(VaultCardDark)
                                .border(1.dp, VaultBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val emptyIcon = when (selectedCategory) {
                                VaultCategory.PHOTO -> Icons.Default.PhotoLibrary
                                VaultCategory.VIDEO -> Icons.Default.VideoLibrary
                                VaultCategory.DOCUMENT -> Icons.Default.Description
                                VaultCategory.NOTE -> Icons.Default.StickyNote2
                                null -> Icons.Default.Lock
                            }
                            Icon(
                                imageVector = emptyIcon,
                                contentDescription = null,
                                tint = VaultPrimary,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        val emptyTitle = when (selectedCategory) {
                            VaultCategory.PHOTO -> "No Hidden Photos"
                            VaultCategory.VIDEO -> "No Hidden Videos"
                            VaultCategory.DOCUMENT -> "No Hidden Files"
                            VaultCategory.NOTE -> "No Secret Notes"
                            null -> "Your Vault is Empty"
                        }

                        Text(
                            text = emptyTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = VaultTextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Tap the 'Hide Files' button below to import and protect photos, videos, or documents away from public galleries.",
                            fontSize = 13.sp,
                            color = VaultTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // Content display
                val isOnlyPhotos = selectedCategory == VaultCategory.PHOTO
                val isOnlyVideos = selectedCategory == VaultCategory.VIDEO

                if (isOnlyPhotos) {
                    // 3-column photo grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items, key = { it.id }) { item ->
                            VaultPhotoOrVideoCard(
                                item = item,
                                onClick = { onItemClick(item) },
                                onUnhide = { onUnhideItem(item) },
                                onDelete = { onDeleteItem(item) },
                                onShare = { onShareItem(item) }
                            )
                        }
                    }
                } else if (isOnlyVideos) {
                    // 2-column video grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items, key = { it.id }) { item ->
                            VaultPhotoOrVideoCard(
                                item = item,
                                onClick = { onItemClick(item) },
                                onUnhide = { onUnhideItem(item) },
                                onDelete = { onDeleteItem(item) },
                                onShare = { onShareItem(item) }
                            )
                        }
                    }
                } else {
                    // Mixed or Documents / Notes list
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items, key = { it.id }) { item ->
                            if (item.category == VaultCategory.PHOTO || item.category == VaultCategory.VIDEO) {
                                VaultDocumentOrNoteCard(
                                    item = item,
                                    onClick = { onItemClick(item) },
                                    onUnhide = { onUnhideItem(item) },
                                    onDelete = { onDeleteItem(item) },
                                    onShare = { onShareItem(item) }
                                )
                            } else {
                                VaultDocumentOrNoteCard(
                                    item = item,
                                    onClick = { onItemClick(item) },
                                    onUnhide = { onUnhideItem(item) },
                                    onDelete = { onDeleteItem(item) },
                                    onShare = { onShareItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add to Vault Options Bottom Sheet
        if (showAddOptionsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddOptionsSheet = false },
                sheetState = sheetState,
                containerColor = VaultCardDark,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(VaultBorder)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Add to Secret Vault",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = VaultTextPrimary
                    )
                    Text(
                        text = "Select what you would like to secure and hide:",
                        fontSize = 12.sp,
                        color = VaultTextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. Photos
                    AddOptionItem(
                        icon = Icons.Default.PhotoLibrary,
                        iconTint = VaultPrimary,
                        title = "Hide Photos",
                        subtitle = "Select photos to remove from gallery & lock",
                        testTag = "hide_photos_option",
                        onClick = {
                            showAddOptionsSheet = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Videos
                    AddOptionItem(
                        icon = Icons.Default.VideoLibrary,
                        iconTint = VaultSecondary,
                        title = "Hide Videos",
                        subtitle = "Select video recordings to quarantine in vault",
                        testTag = "hide_videos_option",
                        onClick = {
                            showAddOptionsSheet = false
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Files & Documents
                    AddOptionItem(
                        icon = Icons.Default.Description,
                        iconTint = VaultPrimary,
                        title = "Hide Files / Documents",
                        subtitle = "PDFs, Word docs, audio, archives, or any file",
                        testTag = "hide_docs_option",
                        onClick = {
                            showAddOptionsSheet = false
                            documentPickerLauncher.launch(arrayOf("*/*"))
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Secret Note
                    AddOptionItem(
                        icon = Icons.Default.StickyNote2,
                        iconTint = VaultTertiary,
                        title = "New Secret Note",
                        subtitle = "Save encrypted text, passwords, or confidential notes",
                        testTag = "hide_note_option",
                        onClick = {
                            showAddOptionsSheet = false
                            onOpenAddNote()
                        }
                    )

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

@Composable
private fun AddOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = VaultCardElevated,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = VaultTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = VaultTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
