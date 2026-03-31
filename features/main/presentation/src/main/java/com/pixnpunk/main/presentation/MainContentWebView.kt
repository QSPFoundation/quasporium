package com.pixnpunk.main.presentation

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MainContentWebView(
    modifier: Modifier = Modifier,
    mainDesc: String,
    backColor: Int,
    paddingValues: PaddingValues,
    setupWebClient: (WebView) -> WebView
) {
    AndroidView(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
        factory = { ctx ->
            setupWebClient(
                WebView(ctx).apply {
                    setBackgroundColor(backColor)
                    layoutParams = ViewGroup.LayoutParams(
                        MATCH_PARENT,
                        MATCH_PARENT
                    )
                }
            )
        },
        update = { webView ->
            if (webView.tag != mainDesc) {
                webView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/game_files/",
                    mainDesc,
                    "text/html", "UTF-8", null
                )
                webView.tag = mainDesc
            }
        }
    )
}