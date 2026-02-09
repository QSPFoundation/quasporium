package com.pixnpunk.extra.presentation

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ExtraContent(
    component: ExtraComponent
) {
    val state by component.model.collectAsState()
    val settings by component.settingsState.collectAsState()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        update = { webView ->
            webView.loadDataWithBaseURL(
                "https://appassets.androidplatform.net/game_files/",
                state.extraDesc,
                "text/html", "UTF-8", null
            )
        },
        factory = { ctx ->
            component.getDefaultWebClient(
                WebView(ctx).apply {
                    setBackgroundColor(settings.backColor)
                    layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    loadDataWithBaseURL(
                        "https://appassets.androidplatform.net/game_files/",
                        state.extraDesc,
                        "text/html", "UTF-8", null
                    )
                }
            )
        }
    )
}