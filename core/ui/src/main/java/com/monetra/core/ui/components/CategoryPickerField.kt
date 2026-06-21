package com.monetra.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class CategoryItem(
    val id: String,
    val icon: ImageVector,
    val name: String,
    val color: Color
)

/**
 * Colored circular icon container — white icon on colored circle.
 * Used both in the picker grid and as the leading icon of the text field.
 */
@Composable
fun CategoryIconBadge(
    icon: ImageVector,
    color: Color,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(iconSize)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPickerField(
    selectedCategoryId: String,
    categories: List<CategoryItem>,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Category"
) {
    var showSheet by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id.equals(selectedCategoryId, ignoreCase = true) }
        ?: categories.firstOrNull()

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedCategory?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = {
                if (selectedCategory != null) {
                    CategoryIconBadge(
                        icon = selectedCategory.icon,
                        color = selectedCategory.color,
                        size = 32.dp,
                        iconSize = 18.dp
                    )
                } else {
                    Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null)
                }
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Category"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Transparent overlay to detect clicks
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { showSheet = true }
        )
    }

    CommonBottomSheet(
        isOpen = showSheet,
        onDismissRequest = { showSheet = false }
    ) {
        // BottomSheetPopup renders in a Dialog window — draws above keyboard natively.
        // dismiss() is available from BottomSheetPopupScope (this scope).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Select Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val chunkedCategories = categories.chunked(3)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                chunkedCategories.forEach { rowCategories ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowCategories.forEach { cat ->
                            val isSelected = cat.id.equals(selectedCategoryId, ignoreCase = true)
                            Card(
                                onClick = {
                                    onCategorySelected(cat.id)
                                    dismiss()
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        cat.color.copy(alpha = 0.10f)
                                    else
                                        MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected)
                                    BorderStroke(2.dp, cat.color)
                                else
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CategoryIconBadge(
                                        icon = cat.icon,
                                        color = cat.color,
                                        size = 44.dp,
                                        iconSize = 24.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected)
                                            cat.color
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        // Fill remaining slots in row for alignment
                        if (rowCategories.size < 3) {
                            repeat(3 - rowCategories.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
