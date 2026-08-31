package com.pixnpunk.webview.presentation

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.pixnpunk.ui.webview.R
import com.pixnpunk.utils.ColorUtil.toHexColorStr
import java.io.File

@Composable
fun GameWebView(
    component: WebViewComponent,
    modifier: Modifier = Modifier
) {
    val state by component.model.collectAsState()

    AndroidView(
        modifier = modifier,
        update = { webView ->
            val contentHash = state.htmlContentHash
            val themeHash = arrayOf(
                state.htmlConfig.backColor,
                state.htmlConfig.textColor,
                state.htmlConfig.linkColor
            ).contentHashCode()

            if (webView.getTag(R.id.tag_content_hash) != contentHash) {
                webView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/game_files/",
                    state.htmlContent,
                    "text/html", "UTF-8", null
                )
                webView.setTag(R.id.tag_content_hash, contentHash)
                webView.setTag(R.id.tag_theme_hash, themeHash)
            } else if (webView.getTag(R.id.tag_theme_hash) != themeHash) {
                webView.evaluateJavascript(
                    """
                        document.documentElement.style.setProperty('--qsp-bg-color', '${state.htmlConfig.backColor.toHexColorStr()}');
                        document.documentElement.style.setProperty('--qsp-text-color', '${state.htmlConfig.textColor.toHexColorStr()}');
                        document.documentElement.style.setProperty('--qsp-link-color', '${state.htmlConfig.linkColor.toHexColorStr()}');
                        document.documentElement.style.setProperty('--qsp-font-size', '${state.htmlConfig.fontSize}');
                    """.trimIndent(),
                    null
                )
                webView.setTag(R.id.tag_theme_hash, themeHash)
            }
        },
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(state.htmlConfig.backColor)
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

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

                setLayerType(View.LAYER_TYPE_HARDWARE, null)

                val density = ctx.resources.displayMetrics.density
                var lastContentHeight = 0

                viewTreeObserver.addOnPreDrawListener {
                    val currentState = component.model.value
                    val currentHeight = contentHeight

                    if (currentHeight != lastContentHeight) {
                        lastContentHeight = currentHeight
                    }

                    if (currentState.isAutoScrollActive) {
                        val targetY = (currentHeight * density).toInt()
                        if (scrollY != targetY) {
                            scrollTo(0, targetY)
                        }
                    } else if (currentState.restoreScrollY > 0) {
                        if (scrollY != currentState.restoreScrollY) {
                            scrollTo(0, currentState.restoreScrollY)
                        }
                    }

                    true
                }

                @SuppressLint("ClickableViewAccessibility")
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        component.onUserTouched()
                    }
                    false
                }

                webViewClient = WebViewClient(
                    assetLoader =
                        WebViewAssetLoader.Builder()
                            .addPathHandler(
                                "/game_files/",
                                ExternalStoragePathHandler(
                                    ctx,
                                    component.gameDirUriBox
                                )
                            )
                            .addPathHandler(
                                "/fonts/",
                                WebViewAssetLoader.InternalStoragePathHandler(
                                    ctx,
                                    File(ctx.filesDir, "custom_fonts")
                                )
                            )
                            .build(),
                    onExecValue = component.onExecValue,
                    onStartActivity = { runCatching { ctx.startActivity(it) } },
                )
            }
        }
    )
}