package com.pixnpunk.webview

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.utils.HtmlUtil.appendPageTemplate
import com.pixnpunk.utils.HtmlUtil.getCleanHtmlAndMedia
import com.pixnpunk.utils.HtmlUtil.getCleanHtmlRemMedia

@Composable
fun GameWebView(
    modifier: Modifier = Modifier,
    rawHtmlContent: String,
    gameDirUriBox: String,
    backColor: Int,
    settingsEntity: SettingsEntity,
    isEnableAutoscroll: Boolean = true,
    onExecValue: (String) -> Unit
) {
    val currentVideoMute by rememberUpdatedState(settingsEntity.isVideoMute)
    val currentAutoscroll by rememberUpdatedState(isEnableAutoscroll)
    val currentContentHash by rememberUpdatedState(rawHtmlContent.hashCode())

    var lastScrolledHash by rememberSaveable { mutableIntStateOf(0) }
    var savedScrollY by rememberSaveable { mutableIntStateOf(0) }

    AndroidView(
        modifier = modifier,
        update = { webView ->
            if (webView.tag != rawHtmlContent) {
                val isNewContent = currentContentHash != lastScrolledHash
                val targetRestoreY = if (!isNewContent) savedScrollY else 0
                val doAutoscroll = isNewContent && currentAutoscroll

                webView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/game_files/",
                    appendPageTemplate(
                        settings = settingsEntity,
                        body = rawHtmlContent,
                        autoscroll = doAutoscroll,
                        restoreScrollY = targetRestoreY
                    ).let { s ->
                        if (settingsEntity.isImgLoadDis) {
                            s.getCleanHtmlRemMedia()
                        } else {
                            s.getCleanHtmlAndMedia(settingsEntity)
                        }
                    },
                    "text/html", "UTF-8", null
                )
                webView.tag = rawHtmlContent

                if (isNewContent) {
                    savedScrollY = 0
                    lastScrolledHash = currentContentHash
                }
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
                    onExecValue = onExecValue,
                    onStartActivity = { runCatching { ctx.startActivity(it) } }
                )
            }
        }
    )
}