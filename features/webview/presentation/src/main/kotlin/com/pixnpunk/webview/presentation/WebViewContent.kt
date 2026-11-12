package com.pixnpunk.webview.presentation

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.pixnpunk.common.ColorUtil.toHexColorStr
import java.io.File
import kotlin.math.roundToInt

@Composable
fun GameWebView(
    component: WebViewComponent,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp
) {
    val state by component.model.collectAsState()
    var rendererCrashes by remember { mutableIntStateOf(0) }

    key(rendererCrashes) {
        AndroidView(
            modifier = modifier,
            onRelease = { webView ->
                webView.stopLoading()
                webView.destroy()
            },
            update = { webView ->
                val contentHash = state.htmlContentHash
                val themeHash = arrayOf(
                    state.htmlConfig.backColor,
                    state.htmlConfig.textColor,
                    state.htmlConfig.linkColor
                ).contentHashCode()
                val bottomInsetPx = bottomInset.value.roundToInt()

                if (webView.contentHash != contentHash) {
                    webView.loadDataWithBaseURL(
                        "https://appassets.androidplatform.net/game_files/",
                        state.htmlContent.withBottomInset(bottomInsetPx),
                        "text/html", "UTF-8", null
                    )
                    webView.contentHash = contentHash
                    webView.themeHash = themeHash
                    webView.bottomInset = bottomInsetPx
                } else if (webView.themeHash != themeHash) {
                    webView.evaluateJavascript(
                        """
                            document.documentElement.style.setProperty('--qsp-bg-color', '${state.htmlConfig.backColor.toHexColorStr()}');
                            document.documentElement.style.setProperty('--qsp-text-color', '${state.htmlConfig.textColor.toHexColorStr()}');
                            document.documentElement.style.setProperty('--qsp-link-color', '${state.htmlConfig.linkColor.toHexColorStr()}');
                            document.documentElement.style.setProperty('--qsp-font-size', '${state.htmlConfig.fontSize}');
                        """.trimIndent(),
                        null
                    )
                    webView.themeHash = themeHash
                }

                if (webView.bottomInset != bottomInsetPx) {
                    webView.evaluateJavascript(
                        bottomInsetScript(bottomInsetPx),
                        null
                    )
                    webView.bottomInset = bottomInsetPx
                }
            },
            factory = { ctx ->
                val density = ctx.resources.displayMetrics.density

                AutoScrollWebView(ctx, onTouchDown = component::onUserTouched) {
                    val currentState = component.model.value

                    if (currentState.isAutoScrollActive) {
                        val targetY = (contentHeight * density).toInt()
                        if (scrollY != targetY) {
                            scrollTo(0, targetY)
                        }
                    } else if (currentState.restoreScrollY > 0) {
                        if (scrollY != currentState.restoreScrollY) {
                            scrollTo(0, currentState.restoreScrollY)
                        }
                    }
                }.apply {
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

                    setOnLongClickListener { view ->
                        if (!component.model.value.htmlConfig.isUseFullscreenImages) {
                            return@setOnLongClickListener false
                        }
                        val hit = (view as WebView).hitTestResult
                        val isImage = hit.type == WebView.HitTestResult.IMAGE_TYPE ||
                                hit.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
                        val path = hit.extra?.takeIf { isImage }?.let(::gameImagePath)
                        path?.let(component.onOpenImage)
                        path != null
                    }

                    webViewClient = WebViewClient(
                        assetLoader = lazy {
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
                                .build()
                        },
                        onExecValue = component.onExecValue,
                        onOpenLink = component.onOpenLink,
                        onOpenImage = component.onOpenImage,
                        onRenderProcessGone = { rendererCrashes++ }
                    )
                }
            }
        )
    }
}
