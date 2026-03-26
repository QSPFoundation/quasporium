package com.pixnpunk.settings.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppBar(
    activeComponent: SettingsComponent.Child,
    scrollBehavior: TopAppBarScrollBehavior,
    rootChildTitle: String,
    onClickBack: () -> Unit
) {
    MediumTopAppBar(
        title = {
            Text(
                when (activeComponent) {
                    is SettingsComponent.Child.RootChild -> rootChildTitle

                    is SettingsComponent.Child.AudioChild -> stringResource(R.string.audio_button_title)

                    is SettingsComponent.Child.GeneralChild -> stringResource(R.string.general_button_title)

                    is SettingsComponent.Child.PicturesChild -> stringResource(R.string.pictures_button_title)

                    is SettingsComponent.Child.TextChild -> stringResource(R.string.text_button_title)
                }
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
                            contentDescription = null
                        )
                    }

                    else -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}