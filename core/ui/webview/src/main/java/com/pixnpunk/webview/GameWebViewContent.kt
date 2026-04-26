package com.pixnpunk.webview

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader

@Composable
fun GameWebView(
    component: GameWebViewComponent,
    modifier: Modifier = Modifier
) {
    val state by component.model.collectAsState()

    AndroidView(
        modifier = modifier,
        update = { webView ->
            if (webView.tag != state.htmlContent) {
                webView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/game_files/",
                    state.htmlContent,
                    "text/html", "UTF-8", null
                )
                webView.tag = state.htmlContent
            }
        },
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(state.htmlConfig.backColor)
                layoutParams = ViewGroup.LayoutParams(
                    MATCH_PARENT,
                    MATCH_PARENT
                )
                settings.apply {
                    allowFileAccess = false
                    javaScriptEnabled = true
                    blockNetworkLoads = true
                    domStorageEnabled = true
                    if (state.htmlConfig.isVideoMute) {
                        settings.mediaPlaybackRequiresUserGesture = false
                    }
                }
                overScrollMode = View.OVER_SCROLL_NEVER
                webViewClient = GameWebViewClient(
                    assetLoader =
                        WebViewAssetLoader.Builder()
                            .addPathHandler(
                                "/game_files/",
                                ExternalStoragePathHandler(
                                    ctx,
                                    component.gameDirUriBox
                                )
                            )
                            .build(),
                    onExecValue = component.onExecValue,
                    onStartActivity = { runCatching { ctx.startActivity(it) } }
                )
            }
        }
    )
}