package com.pixnpunk.settings.presentation.contents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import me.zhanghai.compose.preference.switchPreference

@Composable
fun SettingsAudioContent(
) {
    LazyColumn {
        switchPreference(
            key = "useGameAudioPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null
                )
            },
            defaultValue = true,
            title = { Text("Play game audio") }
        )
        switchPreference(
            key = "useMuteGameVideoPref",
            icon = {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text("Mute game video") }
        )
        switchPreference(
            key = "useGameAudioDebug",
            icon = {
                Icon(
                    imageVector = Icons.Default.AudioFile,
                    contentDescription = null
                )
            },
            defaultValue = false,
            title = { Text("Audio path debugger") }
        )
    }
}