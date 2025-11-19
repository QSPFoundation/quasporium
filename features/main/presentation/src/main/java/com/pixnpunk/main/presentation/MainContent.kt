package com.pixnpunk.main.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    component: MainComponent
) {
    val state by component.model.collectAsState()
    val settings by component.settingsFlow.collectAsState()

    val lazyListState = rememberLazyListState()
    val isLazyColumnAtTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    if (!state.isActionsVis || !state.actions.isNotEmpty()) {
        MainContentWebView(
            mainDesc = state.mainDesc,
            backColor = settings.backColor,
            paddingValues = PaddingValues(),
            setupWebClient = { component.getDefaultWebClient(it) }
        )
    } else {
        BottomSheetScaffold(
            sheetContent = {
                MainContentActionsList(
                    countActsVis = settings.visActsCount,
                    settings = settings,
                    actions = state.actions,
                    onActionClicked = component.onActionClicked,
                    listState = lazyListState
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
