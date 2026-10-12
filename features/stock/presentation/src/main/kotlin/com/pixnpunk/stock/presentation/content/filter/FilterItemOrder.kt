package com.pixnpunk.stock.presentation.content.filter

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.ui.res.stringResource
import com.pixnpunk.stock.domain.model.SortOrder
import com.pixnpunk.stock.presentation.R

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
                contentDescription = stringResource(
                    if (sortOrder == SortOrder.ASCENDING) R.string.sort_ascending_description
                    else R.string.sort_descending_description
                )
            )
        }
    )
}