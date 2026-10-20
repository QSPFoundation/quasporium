package com.pixnpunk.settings.presentation.contents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Height
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.pixnpunk.dto.AppThemeMode
import com.pixnpunk.dto.BinaryPrefFormat
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.settings.domain.model.PrefKeys
import com.pixnpunk.settings.presentation.R
import me.zhanghai.compose.preference.listPreference
import me.zhanghai.compose.preference.switchPreference

@Composable
fun SettingsGeneralContent(
    isLocalSettings: Boolean
) = LazyColumn {
    if (!isLocalSettings) {
        listPreference(
            key = PrefKeys.APP_THEME,
            icon = {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null
                )
            },
            defaultValue = AppThemeMode.SYSTEM.toString(),
            values = AppThemeMode.entries.map { it.toString() },
            valueToText = {
                AnnotatedString(
                    when (AppThemeMode.valueOf(it)) {
                        AppThemeMode.SYSTEM -> "System"
                        AppThemeMode.LIGHT -> "Light"
                        AppThemeMode.DARK -> "Dark"
                    }
                )
            },
            title = { Text(stringResource(R.string.theme_list_title)) }
        )
    }
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
        key = PrefKeys.LIB_VERSION,
        icon = {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null
            )
        },
        defaultValue = NativeLibAuthors.SEEDHARTHA.name,
        values = NativeLibAuthors.entries.map { it.toString() },
        valueToText = {
            AnnotatedString(
                when (NativeLibAuthors.valueOf(it)) {
                    NativeLibAuthors.SEEDHARTHA -> "5.7.0 (by seedHarta)"
                    NativeLibAuthors.BYTE -> "5.9.5 (by Byte)"
                }
            )
        },
        title = {
            Text(
                if (isLocalSettings) {
                    stringResource(R.string.local_library_version_list_title)
                } else {
                    stringResource(R.string.global_library_version_list_title)
                }
            )
        }
    )
    listPreference(
        key = PrefKeys.BINARY_PREF,
        icon = {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null
            )
        },
        defaultValue = BinaryPrefFormat.SI.name,
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
        key = PrefKeys.ACTS_COUNT,
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
        key = PrefKeys.IMMERSIVE_MODE,
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