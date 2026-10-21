package com.pixnpunk.stock.presentation.content.search

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pixnpunk.stock.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchInputField(
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    searchQuery: String,
    isSearchExpanded: Boolean,
    updateSearchQuery: (String) -> Unit,
    executeSearch: () -> Unit,
    toggleSearch: (Boolean) -> Unit,
    navigateToSettings: () -> Unit
) = SearchBarDefaults.InputField(
    searchBarState = searchBarState,
    textFieldState = textFieldState,
    onSearch = { executeSearch() },
    placeholder = { Text("Search games") },
    leadingIcon = {
        if (!isSearchExpanded) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        } else {
            IconButton(onClick = { toggleSearch(false) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button_description)
                )
            }
        }
    },
    trailingIcon = {
        if (isSearchExpanded && searchQuery.isNotEmpty()) {
            IconButton(onClick = { updateSearchQuery("") }) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.clear_query_button_description)
                )
            }
        } else if (!isSearchExpanded) {
            Row {
                IconButton(onClick = navigateToSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_button_description)
                    )
                }
            }
        }
    }
)
