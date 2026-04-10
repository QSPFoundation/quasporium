package com.pixnpunk.main.presentation

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
import com.pixnpunk.webview.GameWebView

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
        if (!state.isActionsVis || state.actions.isEmpty()) {
                GameWebView(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                    htmlContent = state.mainDesc,
                    backColor = settings.backColor,
                    isVideoMute = settings.isVideoMute,
                    isEnableAutoscroll = settings.isYesScroll,
                    gameDirUriBox = component.gameDirUriBox,
                    onExecValue = component.onExecValue
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
                GameWebView(
                    modifier = Modifier
                        .padding(sheetValues)
                        .fillMaxSize(),
                    htmlContent = state.mainDesc,
                    backColor = settings.backColor,
                    isVideoMute = settings.isVideoMute,
                    isEnableAutoscroll = settings.isYesScroll,
                    gameDirUriBox = component.gameDirUriBox,
                    onExecValue = component.onExecValue
                )
            }
        }
    }
}
