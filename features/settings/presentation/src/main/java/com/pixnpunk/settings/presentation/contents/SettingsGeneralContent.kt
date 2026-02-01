package com.pixnpunk.settings.presentation.contents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.settings.presentation.R
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.switchPreference

@Composable
fun SettingsGeneralContent(
) {
    LazyColumn {
        listPreference(
            key = "appThemePref",
            icon = {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null
                )
            },
            defaultValue = "auto",
            values = listOf("auto", "light", "dark"),
            valueToText = {
                AnnotatedString(
                    it.replaceFirstChar { txt ->
                        if (txt.isLowerCase()) txt.titlecase() else it
                    }
                )
            },
            title = { Text(stringResource(R.string.theme_list_title)) }
        )
//        listPreference(
//            key = "appLangPref",
//            icon = {
//                Icon(
//                    imageVector = Icons.Default.Language,
//                    contentDescription = null
//                )
//            },
//            defaultValue = "en",
//            values = listOf("en", "ru"),
//            valueToText = {
//                AnnotatedString(
//                    when (it) {
//                        "en" -> "English"
//                        "ru" -> "Russian"
//                        else -> it
//                    }
//                )
//            },
//            title = { Text("Application language") }
//        )
        listPreference(
            key = "appLibVerPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null
                )
            },
            defaultValue = NativeLibAuthors.SEEDHARTHA.toString(),
            values = NativeLibAuthors.entries.map { it.toString() },
            valueToText = {
                AnnotatedString(
                    when (NativeLibAuthors.valueOf(it)) {
                        NativeLibAuthors.SEEDHARTHA -> "5.7.0 (by seedHarta)"
                        NativeLibAuthors.SONNIX -> "5.8.0 (by Sonnix)"
                        NativeLibAuthors.BYTE -> "5.9.3 (by Byte)"
                    }
                )
            },
            title = { Text(stringResource(R.string.library_version_list_title)) }
        )
        listPreference(
            key = "appBinPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null
                )
            },
            defaultValue = BinaryPrefFormat.SI.toString(),
            values = BinaryPrefFormat.entries.map { it.toString() },
            valueToText = {
                AnnotatedString(
                    when (BinaryPrefFormat.valueOf(it)) {
                        BinaryPrefFormat.SI -> "B/KB/MB/GB/TB"
                        BinaryPrefFormat.ISO -> "B/KiB/MiB/GiB/TiB"
                    }
                )
            },
            title = { Text(stringResource(R.string.prefixes_list_title)) }
        )
        listPreference(
            key = "countActsPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.Height,
                    contentDescription = null
                )
            },
            defaultValue = 3,
            values = listOf(3, 4, 5, 6),
            title = { Text(stringResource(R.string.count_action_list_title)) }
        )
        switchPreference(
            key = "appImmersiveModePref",
            icon = {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = null
                )
            },
            defaultValue = true,
            title = { Text(stringResource(R.string.immersive_switch_title)) }
        )
    }
}