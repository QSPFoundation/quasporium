package com.pixnpunk.stock.presentation.content.filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.pixnpunk.stock.domain.model.SearchTokenType
import com.pixnpunk.stock.presentation.model.SearchTokenUI

fun LazyListScope.filterItemToken(
    activeTokens: List<SearchTokenUI>,
    removeToken: (SearchTokenUI) -> Unit
) = items(activeTokens) { token ->
    InputChip(
        selected = true,
        onClick = { },
        label = { Text(token.displayText) },
        leadingIcon = {
            Icon(
                imageVector = when (token.type) {
                    SearchTokenType.AUTHOR -> Icons.Default.Person
                    SearchTokenType.TRANSLATOR -> Icons.Default.Translate
                    SearchTokenType.TITLE -> Icons.Default.Book
                    SearchTokenType.DESCRIPTION -> Icons.AutoMirrored.Filled.Subject
                },
                contentDescription = null,
                modifier = Modifier.size(InputChipDefaults.IconSize)
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier
                    .size(InputChipDefaults.IconSize)
                    .clickable { removeToken(token) }
            )
        }
    )
}