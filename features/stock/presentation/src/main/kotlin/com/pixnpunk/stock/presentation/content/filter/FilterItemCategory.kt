package com.pixnpunk.stock.presentation.content.filter

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.pixnpunk.stock.domain.model.SortBy
import com.pixnpunk.stock.presentation.R

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
                stringResource(
                    when (option) {
                        SortBy.CREATED -> R.string.stock_sort_created
                        SortBy.UPDATED -> R.string.stock_sort_updated
                        SortBy.COMMENTS -> R.string.stock_sort_comments
                        SortBy.LIKES -> R.string.stock_sort_likes
                        SortBy.DOWNLOADS -> R.string.stock_sort_downloads
                        SortBy.PLAYS -> R.string.stock_sort_plays
                        SortBy.EMPTY -> R.string.stock_sort_none
                    }
                )
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
