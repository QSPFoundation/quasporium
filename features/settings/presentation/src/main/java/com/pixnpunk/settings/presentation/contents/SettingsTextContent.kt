package com.pixnpunk.settings.presentation.contents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.FontTypeface
import com.pixnpunk.settings.presentation.R
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.switchPreference
import me.zhanghai.compose.preference.twoTargetSwitchPreference

@Composable
fun SettingsTextContent(
    showColorDialog: (ColorPickerState) -> Unit
) {
    LazyColumn {
        listPreference(
            key = "fontStylePref",
            icon = {
                Icon(
                    imageVector = Icons.Default.TextFormat,
                    contentDescription = null
                )
            },
            defaultValue = FontTypeface.DEFAULT.toString(),
            values = FontTypeface.entries.map { it.toString() },
            valueToText = {
                AnnotatedString(
                    when (FontTypeface.valueOf(it)) {
                        FontTypeface.DEFAULT -> "Default"
                        FontTypeface.SANS_SERIF -> "Sans-Serif"
                        FontTypeface.SERIF -> "Serif"
                        FontTypeface.MONOSPACE -> "Monospace"
                    }
                )
            },
            title = { Text(stringResource(R.string.font_style_list_title)) }
        )
        listPreference(
            key = "fontSizePref",
            icon = {
                Icon(
                    imageVector = Icons.Default.FormatSize,
                    contentDescription = null
                )
            },
            defaultValue = 16,
            values = listOf(12, 14, 16, 18, 20, 22),
            title = { Text(stringResource(R.string.font_size_list_title)) }
        )
        twoTargetSwitchPreference(
            key = "customTextColorPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.BorderColor,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text(stringResource(R.string.font_color_switch_title)) },
            summary = {
                if (it) {
                    Text(stringResource(R.string.font_color_switch_summary))
                }
            },
            onClick = {
                if (it) {
                    showColorDialog(ColorPickerState.COLOR_TEXT)
                }
            }
        )
        twoTargetSwitchPreference(
            key = "customBackColorPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.FormatColorFill,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text(stringResource(R.string.back_color_switch_title)) },
            summary = {
                if (it) {
                    Text(stringResource(R.string.back_color_switch_summary))
                }
            },
            onClick = {
                if (it) {
                    showColorDialog(ColorPickerState.COLOR_BACK)
                }
            }
        )
        twoTargetSwitchPreference(
            key = "customLinkColorPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.FormatPaint,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text(stringResource(R.string.link_color_switch_title)) },
            summary = {
                if (it) {
                    Text(stringResource(R.string.link_color_switch_summary))
                }
            },
            onClick = {
                if (it) {
                    showColorDialog(ColorPickerState.COLOR_LINK)
                }
            }
        )
        switchPreference(
            key = "useGameExecStringPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text(stringResource(R.string.string_exec_switch_title)) }
        )
    }
}