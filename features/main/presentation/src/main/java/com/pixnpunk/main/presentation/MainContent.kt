package com.pixnpunk.main.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    component: MainComponent,
    topAppBar: @Composable (() -> Unit)
) {
    val state by component.model.collectAsState()
    val settings by component.settingsState.collectAsState()

    val lazyStaggeredGridState = rememberLazyStaggeredGridState()
    val isLazyColumnAtTop by remember {
        derivedStateOf {
            lazyStaggeredGridState.firstVisibleItemIndex == 0
                    && lazyStaggeredGridState.firstVisibleItemScrollOffset == 0
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = topAppBar
    ) { paddingValues ->
        if (!state.isActionsVis || !state.actions.isNotEmpty()) {
            MainContentWebView(
                modifier = Modifier.padding(paddingValues),
                mainDesc = state.mainDesc,
                backColor = settings.backColor,
                paddingValues = PaddingValues(),
                setupWebClient = component::getDefaultWebClient
            )
        } else {
            BottomSheetScaffold(
                modifier = Modifier.padding(paddingValues),
                sheetContent = {
                    MainContentActionsList(
                        countActsVis = settings.visActsCount,
                        settings = settings,
                        actions = state.actions,
                        onActionClicked = component.onActionClicked,
                        gridState = lazyStaggeredGridState
                    )
                },
                sheetSwipeEnabled = isLazyColumnAtTop
            ) { sheetValues ->
                MainContentWebView(
                    mainDesc = state.mainDesc,
                    backColor = settings.backColor,
                    paddingValues = sheetValues,
                    setupWebClient = { component.getDefaultWebClient(it) }
                )
            }
        }
    }
}
