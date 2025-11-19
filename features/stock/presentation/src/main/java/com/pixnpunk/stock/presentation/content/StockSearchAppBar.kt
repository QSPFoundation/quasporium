package com.pixnpunk.stock.presentation.content

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pixnpunk.stock.presentation.RealStockComponent
import com.pixnpunk.stock.presentation.StockComponent
import com.pixnpunk.stock.presentation.TabState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockSearchAppBar(
    component: StockComponent
) {
    var innerTabState by rememberSaveable { mutableIntStateOf(TabState.INSTALLED.ordinal) }
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
                    onSearch = { component.searchEntries(it) },
                    expanded = false,
                    onExpandedChange = { },
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = component.navigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = ""
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
            selectedTabIndex = innerTabState,
        ) {
            TabState.entries.forEachIndexed { index, state ->
                Tab(
                    selected = innerTabState == index,
                    onClick = {
                        innerTabState = index
                        component.navigateTo(
                            when (state) {
                                TabState.INSTALLED -> RealStockComponent.ChildConfig.Installed
                                TabState.LIBRARY -> RealStockComponent.ChildConfig.Library
                            }
                        )
                    },
                    text = {
                        Text(
                            text = state.name,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                )
            }
        }
    }
}