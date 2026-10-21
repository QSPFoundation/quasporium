package com.pixnpunk.details.presentation.content

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.window.core.layout.WindowSizeClass
import com.pixnpunk.details.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsAppBar(
    isPlayable: Boolean,
    onDismissed: () -> Unit,
    onShowSettings: () -> Unit
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(
                onClick = onDismissed
            ) {
                if (
                    currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(
                        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.back_button_description)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button_description)
                    )
                }
            }
        },
        actions = {
            if (isPlayable) {
                IconButton(
                    onClick = onShowSettings
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = stringResource(R.string.settings_button_description)
                    )
                }
            }
        }
    )
}
