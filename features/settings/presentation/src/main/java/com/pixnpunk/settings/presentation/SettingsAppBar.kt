package com.pixnpunk.settings.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppBar(
    activeComponent: SettingsComponent.Child,
    scrollBehavior: TopAppBarScrollBehavior,
    onClickBack: () -> Unit
) {
    MediumTopAppBar(
        title = {
            Text(
                when (activeComponent) {
                    is SettingsComponent.Child.AudioChild -> "Audio"
                    is SettingsComponent.Child.GeneralChild -> "Basic"
                    is SettingsComponent.Child.PicturesChild -> "Pictures"
                    is SettingsComponent.Child.RootChild -> "Settings"
                    is SettingsComponent.Child.TextChild -> "Text"
                }
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onClickBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = ""
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}