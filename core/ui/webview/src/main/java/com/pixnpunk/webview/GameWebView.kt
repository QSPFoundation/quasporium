package com.pixnpunk.webview

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader

@Composable
fun GameWebView(
    modifier: Modifier = Modifier,
    htmlContent: String,
    gameDirUriBox: String,
    backColor: Int,
    isVideoMute: Boolean = true,
    isEnableAutoscroll: Boolean = true,
    onExecValue: (String) -> Unit
) {
    val currentVideoMute by rememberUpdatedState(isVideoMute)
    val currentAutoscroll by rememberUpdatedState(isEnableAutoscroll)

    AndroidView(
        modifier = modifier,
        update = { webView ->
            if (webView.tag != htmlContent) {
                webView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/game_files/",
                    htmlContent,
                    "text/html", "UTF-8", null
                )
                webView.tag = htmlContent
            }
        },
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(backColor)
                layoutParams = ViewGroup.LayoutParams(
                    MATCH_PARENT,
                    MATCH_PARENT
                )
                settings.apply {
                    allowFileAccess = false
                    javaScriptEnabled = true
                    blockNetworkLoads = true
                    domStorageEnabled = true
                    if (currentVideoMute) {
                        settings.mediaPlaybackRequiresUserGesture = false
                    }
                }
                overScrollMode = View.OVER_SCROLL_NEVER
                webViewClient = GameWebViewClient(
                    assetLoader =
                        WebViewAssetLoader.Builder()
                            .addPathHandler(
                                "/game_files/",
                                ExternalStoragePathHandler(ctx, gameDirUriBox)
                            )
                            .build(),
                    isEnableAutoscroll = { currentAutoscroll },
                    onExecValue = onExecValue,
                    onStartActivity = { runCatching { ctx.startActivity(it) } }
                )
            }
        }
    )
}