package com.pixnpunk.main.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
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
    val hasActions = state.isActionsVis && state.actions.isNotEmpty()

    val parsedActions = remember(state.actions) {
        if (!hasActions) emptyList()
        else state.actions.map { item ->
            ParsedActionItem(
                original = item,
                annotatedText = AnnotatedString.fromHtml(item.text),
                hasImage = item.imagePath.isNotBlank()
            )
        }
    }

    val isSwipeEnabled by remember {
        derivedStateOf {
            !lazyStaggeredGridState.canScrollBackward &&
                    !lazyStaggeredGridState.canScrollForward
        }
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
                    countActsVis = settings.visActsCount,
                    settings = settings,
                    actions = parsedActions,
                    onActionClicked = component.onActionClicked,
                    gridState = lazyStaggeredGridState
                )
            } else {
                // Stub. Prevents measurement crashes in some versions of Material 3
                Box(modifier = Modifier.fillMaxWidth().height(1.dp))
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
