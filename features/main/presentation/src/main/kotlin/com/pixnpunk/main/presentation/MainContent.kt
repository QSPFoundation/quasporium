package com.pixnpunk.main.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pixnpunk.main.presentation.content.MainContentActionsList
import com.pixnpunk.webview.presentation.GameWebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    component: MainComponent,
    topAppBar: @Composable (() -> Unit),
    snackbarHostState: SnackbarHostState
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    val hasActions = uiState.isActionsVis && uiState.parsedActions.isNotEmpty()
    val scrollState = rememberScrollState()

    val isSwipeEnabled by remember {
        derivedStateOf { scrollState.value == 0 }
    }

    BottomSheetScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = topAppBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetPeekHeight = if (hasActions) BottomSheetDefaults.SheetPeekHeight else 0.dp,
        sheetSwipeEnabled = if (hasActions) isSwipeEnabled else false,
        sheetContent = {
            if (hasActions) {
                MainContentActionsList(
                    countActsVis = uiState.visActsCount,
                    uiState = uiState,
                    fontFamily = uiState.fontFamily,
                    actions = uiState.parsedActions,
                    onActionClicked = component.onActionClicked,
                    scrollState = scrollState
                )
            }
        }
    ) { paddingValues ->
        GameWebView(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            component = component.gameWebViewComponent
        )
    }
}