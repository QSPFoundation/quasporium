package com.pixnpunk.settings.presentation.contents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.pixnpunk.dto.InputState
import me.zhanghai.compose.preference.switchPreference
import me.zhanghai.compose.preference.twoTargetSwitchPreference

@Composable
fun SettingsImageContent(
    showInputDialog: (InputState) -> Unit
) {
    LazyColumn {
        switchPreference(
            key = "useGameImagesPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text("Disable game images") }
        )
        twoTargetSwitchPreference(
            key = "useAutoImageWidthPref",
            icon = {
//                Icon(
//                    imageVector = Icons.Default.WidthFull,
//                    contentDescription = null
//                )
            },
            defaultValue = true,
            title = { Text("${if (it) "Auto" else "Manual"} game image width") },
            summary = {
                if (!it) {
                    Text("Specify game image width")
                }
            },
            onClick = { if (!it) { showInputDialog(InputState.INPUT_IMAGE_WIDTH) } }
        )
        twoTargetSwitchPreference(
            key = "useAutoImageHeightPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.Height,
                    contentDescription = null
                )
            },
            defaultValue = true,
            title = { Text("${if (it) "Auto" else "Manual"} game image height") },
            summary = {
                if (!it) {
                    Text("Specify game image height")
                }
            },
            onClick = { if (!it) { showInputDialog(InputState.INPUT_IMAGE_HEIGHT) } }
        )
        switchPreference(
            key = "useImmersImagesPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text("Use dialog for game images ") }
        )
        switchPreference(
            key = "useGameImageDebug",
            icon = {
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text("Image path debugger") }
        )
    }
}