package com.pixnpunk.extra.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pixnpunk.webview.GameWebView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraContent(
    component: ExtraComponent,
    dismiss: () -> Unit
) {
    val state by component.model.collectAsState()
    val settings by component.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Extra") },
                navigationIcon = {
                    IconButton(onClick = dismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        GameWebView(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            htmlContent = state.extraDesc,
            backColor = settings.backColor,
            isVideoMute = settings.isVideoMute,
            isEnableAutoscroll = settings.isYesScrollExtra,
            gameDirUriBox = component.gameDirUriBox,
            onExecValue = component.onExecValue
        )
    }
}