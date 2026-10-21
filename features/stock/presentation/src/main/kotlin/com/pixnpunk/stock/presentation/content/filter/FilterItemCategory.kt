package com.pixnpunk.stock.presentation.content.filter

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.pixnpunk.stock.domain.model.SortBy

fun LazyListScope.filterItemCategory(
    sortBy: SortBy,
    selectSort: (SortBy) -> Unit
) = items(SortBy.entries.filter { it != SortBy.EMPTY }) { option ->
    val isSelected = sortBy == option
    FilterChip(
        selected = isSelected,
        onClick = { selectSort(if (isSelected) SortBy.EMPTY else option) },
        label = {
            Text(
                option.name.lowercase()
                    .replaceFirstChar { it.uppercase() }
                    .replace("_", " ")
            )
        },
        leadingIcon =
            if (!isSelected) {
                null
            } else {
                { Icon(Icons.Default.Check, null) }
            }
    )
}
