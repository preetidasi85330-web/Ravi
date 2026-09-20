package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VaultCategory
import com.example.ui.theme.VaultBorder
import com.example.ui.theme.VaultCardDark
import com.example.ui.theme.VaultPrimary
import com.example.ui.theme.VaultTextPrimary
import com.example.ui.theme.VaultTextSecondary

@Composable
fun CategoryTabs(
    selectedCategory: VaultCategory?,
    onSelectCategory: (VaultCategory?) -> Unit,
    photosCount: Int,
    videosCount: Int,
    docsCount: Int,
    notesCount: Int,
    modifier: Modifier = Modifier
) {
    val totalAll = photosCount + videosCount + docsCount + notesCount
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        CategoryChip(
            title = "All ($totalAll)",
            isSelected = selectedCategory == null,
            icon = Icons.Default.Apps,
            onClick = { onSelectCategory(null) },
            testTag = "category_chip_all"
        )

        // Photos chip
        CategoryChip(
            title = "Photos ($photosCount)",
            isSelected = selectedCategory == VaultCategory.PHOTO,
            icon = Icons.Default.PhotoLibrary,
            onClick = { onSelectCategory(VaultCategory.PHOTO) },
            testTag = "category_chip_photos"
        )

        // Videos chip
        CategoryChip(
            title = "Videos ($videosCount)",
            isSelected = selectedCategory == VaultCategory.VIDEO,
            icon = Icons.Default.VideoLibrary,
            onClick = { onSelectCategory(VaultCategory.VIDEO) },
            testTag = "category_chip_videos"
        )

        // Files chip
        CategoryChip(
            title = "Files ($docsCount)",
            isSelected = selectedCategory == VaultCategory.DOCUMENT,
            icon = Icons.Default.Description,
            onClick = { onSelectCategory(VaultCategory.DOCUMENT) },
            testTag = "category_chip_docs"
        )

        // Notes chip
        CategoryChip(
            title = "Notes ($notesCount)",
            isSelected = selectedCategory == VaultCategory.NOTE,
            icon = Icons.Default.StickyNote2,
            onClick = { onSelectCategory(VaultCategory.NOTE) },
            testTag = "category_chip_notes"
        )
    }
}

@Composable
private fun CategoryChip(
    title: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color(0xFF00363D) else VaultPrimary
            )
        },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = VaultPrimary,
            selectedLabelColor = Color(0xFF00363D),
            containerColor = VaultCardDark,
            labelColor = VaultTextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = VaultBorder,
            selectedBorderColor = VaultPrimary
        ),
        modifier = Modifier.testTag(testTag)
    )
}
