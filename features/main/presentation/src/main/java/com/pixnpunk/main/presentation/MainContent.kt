package com.pixnpunk.main.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import com.pixnpunk.main.presentation.model.ParsedActionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    component: MainComponent,
    topAppBar: @Composable (() -> Unit),
    snackbarHostState: SnackbarHostState
) {
    val state by component.model.collectAsState()
    val settings by component.settingsState.collectAsState()

    val lazyStaggeredGridState = rememberLazyStaggeredGridState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = topAppBar,
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
            val parsedActions = remember(state.actions) {
                state.actions.map { item ->
                    ParsedActionItem(
                        original = item,
                        annotatedText = AnnotatedString.fromHtml(item.text),
                        hasImage = item.imagePath.isNotBlank()
                    )
                }
            }

            BottomSheetScaffold(
                modifier = Modifier.padding(paddingValues),
                sheetContent = {
                    MainContentActionsList(
                        countActsVis = settings.visActsCount,
                        settings = settings,
                        actions = parsedActions,
                        onActionClicked = component.onActionClicked,
                        gridState = lazyStaggeredGridState
                    )
                },
                sheetSwipeEnabled =
                    !lazyStaggeredGridState.canScrollBackward
                            && !lazyStaggeredGridState.canScrollForward
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
