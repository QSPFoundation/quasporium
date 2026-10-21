package com.pixnpunk.stock.presentation.content.filter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

fun LazyListScope.filterItemLang(
    selectedLang: String?,
    selectLang: (String?) -> Unit
) = item {
    var isLangMenuExpanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selectedLang != null,
            onClick = { isLangMenuExpanded = true },
            label = { Text(selectedLang?.uppercase() ?: "Language") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
        )
        DropdownMenu(
            expanded = isLangMenuExpanded,
            onDismissRequest = { isLangMenuExpanded = false }
        ) {
            DropdownMenuItem(text = { Text("Any (Clear)") }, onClick = {
                selectLang(null)
                isLangMenuExpanded = false
            })
            DropdownMenuItem(text = { Text("EN") }, onClick = {
                selectLang("en")
                isLangMenuExpanded = false
            })
            DropdownMenuItem(text = { Text("RU") }, onClick = {
                selectLang("ru")
                isLangMenuExpanded = false
            })
        }
    }
}
