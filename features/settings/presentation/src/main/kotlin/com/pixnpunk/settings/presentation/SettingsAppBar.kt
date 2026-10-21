package com.pixnpunk.settings.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.window.core.layout.WindowSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppBar(
    activeComponent: SettingsComponent.Child,
    scrollBehavior: TopAppBarScrollBehavior,
    isLocalSettings: Boolean,
    gameTitle: String,
    onClickBack: () -> Unit
) {
    MediumTopAppBar(
        title = {
            Text(
                text = when (activeComponent) {
                    is SettingsComponent.Child.RootChild ->
                        if (isLocalSettings) {
                            stringResource(R.string.local_root_button_title, gameTitle)
                        } else {
                            stringResource(R.string.global_root_button_title)
                        }

                    is SettingsComponent.Child.AudioChild -> stringResource(R.string.audio_button_title)

                    is SettingsComponent.Child.GeneralChild -> stringResource(R.string.general_button_title)

                    is SettingsComponent.Child.PicturesChild -> stringResource(R.string.pictures_button_title)

                    is SettingsComponent.Child.TextChild -> stringResource(R.string.text_button_title)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onClickBack
            ) {
                when {
                    isLocalSettings && currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(
                        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                    ) -> {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close_button_description)
                        )
                    }

                    else -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}
