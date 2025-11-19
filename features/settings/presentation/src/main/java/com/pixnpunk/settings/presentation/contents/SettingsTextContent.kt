package com.pixnpunk.settings.presentation.contents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.FontTypeface
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
            title = { Text("Font style") }
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
            title = { Text("Font size") }
        )
        twoTargetSwitchPreference(
            key = "useGameTextColorPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.BorderColor,
                    contentDescription = null
                )
            },
            defaultValue = true,
            title = { Text("Text color ${if (it) "from game" else "from user"}") },
            summary = { if (!it) { Text("Specify text color") } },
            onClick = { if (!it) { showColorDialog(ColorPickerState.COLOR_TEXT) } }
        )
        twoTargetSwitchPreference(
            key = "useGameBackColorPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.FormatColorFill,
                    contentDescription = null
                )
            },
            defaultValue = true,
            title = { Text("Background color ${if (it) "from game" else "from user"}") },
            summary = { if (!it) { Text("Specify background color") } },
            onClick = { if (!it) { showColorDialog(ColorPickerState.COLOR_BACK) } }
        )
        twoTargetSwitchPreference(
            key = "useGameLinkColorPref",
            icon = {
//                Icon(
//                    imageVector = Icons.Default.FormatHi,
//                    contentDescription = null
//                )
            },
            defaultValue = true,
            title = { Text("Link color from ${if (it) "from game" else "from user"}") },
            summary = { if (!it) { Text("Specify link color") } },
            onClick = { if (!it) { showColorDialog(ColorPickerState.COLOR_LINK) } }
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
            title = { Text("Use command line") }
        )
    }
}