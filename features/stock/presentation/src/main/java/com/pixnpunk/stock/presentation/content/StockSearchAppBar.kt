package com.pixnpunk.stock.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pixnpunk.stock.presentation.R
import com.pixnpunk.stock.presentation.TabState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockSearchAppBar(
    pagerState: PagerState,
    searchEntries: (String) -> Unit,
    navigateToSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var searchText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SearchBar(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchText,
                    onQueryChange = { searchText = it },
                    onSearch = { searchEntries(it) },
                    expanded = false,
                    onExpandedChange = { },
                    placeholder = { Text(stringResource(R.string.placeholder_searchbar)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = navigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null
                            )
                        }
                    }
                )
            },
            expanded = false,
            onExpandedChange = { },
            content = { }
        )
        PrimaryTabRow(
            modifier = Modifier.padding(top = 10.dp),
            selectedTabIndex = pagerState.currentPage,
        ) {
            TabState.entries.forEachIndexed { index, state ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = when (state.ordinal) {
                                TabState.INSTALLED.ordinal -> stringResource(R.string.first_tab_text)
                                TabState.LIBRARY.ordinal  -> stringResource(R.string.second_tab_text)
                                else -> ""
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                )
            }
        }
    }
}