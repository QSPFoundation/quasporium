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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import com.pixnpunk.dto.InputState
import com.pixnpunk.settings.dto.model.PrefKeys
import com.pixnpunk.settings.presentation.R
import me.zhanghai.compose.preference.switchPreference
import me.zhanghai.compose.preference.twoTargetSwitchPreference

@Composable
fun SettingsImageContent(
    showInputDialog: (InputState) -> Unit
) {
    LazyColumn {
        switchPreference(
            key = PrefKeys.IMG_LOAD_DISABLE,
            icon = {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text(stringResource(R.string.disable_images_switch_title)) }
        )
        twoTargetSwitchPreference(
            key = PrefKeys.IMG_AUTO_WIDTH,
            icon = {
                Icon(
                    modifier = Modifier.rotate(90f),
                    imageVector = Icons.Default.Height,
                    contentDescription = null
                )
            },
            defaultValue = true,
            title = {
                Text(
                    stringResource(
                        R.string.image_width_twotarswitch_title,
                        if (it) "Auto" else "Manual"
                    )
                )
            },
            summary = {
                if (!it) {
                    Text(stringResource(R.string.image_width_twotarswitch_summary))
                }
            },
            onClick = {
                if (!it) {
                    showInputDialog(InputState.INPUT_IMAGE_WIDTH)
                }
            }
        )
        twoTargetSwitchPreference(
            key = PrefKeys.IMG_AUTO_HEIGHT,
            icon = {
                Icon(
                    imageVector = Icons.Default.Height,
                    contentDescription = null
                )
            },
            defaultValue = true,
            title = {
                Text(
                    stringResource(
                        R.string.image_height_twotarswitch_title,
                        if (it) "Auto" else "Manual"
                    )
                )
            },
            summary = {
                if (!it) {
                    Text(stringResource(R.string.image_height_twotarswitch_summary))
                }
            },
            onClick = {
                if (!it) {
                    showInputDialog(InputState.INPUT_IMAGE_HEIGHT)
                }
            }
        )
        switchPreference(
            key = PrefKeys.IMG_IMMERSIVE,
            icon = {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text(stringResource(R.string.dialog_images_switch_title)) }
        )
        switchPreference(
            key = PrefKeys.IMG_DEBUG,
            icon = {
                Icon(
                    imageVector = Icons.Default.BrokenImage,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text(stringResource(R.string.image_path_switch_title)) }
        )
    }
}