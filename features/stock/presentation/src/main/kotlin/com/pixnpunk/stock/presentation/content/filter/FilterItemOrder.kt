package com.pixnpunk.stock.presentation.content.filter

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import com.pixnpunk.stock.domain.model.SortOrder

fun LazyListScope.filterItemOrder(
    sortOrder: SortOrder,
    toggleSortOrder: () -> Unit
) = item {
    FilterChip(
        selected = false,
        onClick = toggleSortOrder,
        label = {
            Icon(
                imageVector =
                    if (sortOrder == SortOrder.ASCENDING) {
                        Icons.Default.ArrowUpward
                    } else {
                        Icons.Default.ArrowDownward
                    },
                contentDescription = null
            )
        }
    )
}