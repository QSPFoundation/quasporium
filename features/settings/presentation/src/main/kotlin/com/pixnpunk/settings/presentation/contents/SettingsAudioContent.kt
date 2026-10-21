package com.pixnpunk.settings.presentation.contents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pixnpunk.settings.domain.model.PrefKeys
import com.pixnpunk.settings.presentation.R
import me.zhanghai.compose.preference.switchPreference

@Composable
fun SettingsAudioContent() = LazyColumn {
    switchPreference(
        key = PrefKeys.AUDIO_ENABLED,
        icon = {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null
            )
        },
        defaultValue = true,
        title = { Text(stringResource(R.string.audio_play_switch_title)) }
    )
    switchPreference(
        key = PrefKeys.VIDEO_MUTE,
        icon = {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null
            )
        },
        defaultValue = false,
        title = { Text(stringResource(R.string.mute_video_switch_title)) }
    )
    switchPreference(
        key = PrefKeys.AUDIO_DEBUG,
        icon = {
            Icon(
                imageVector = Icons.Default.AudioFile,
                contentDescription = null
            )
        },
        defaultValue = false,
        title = { Text(stringResource(R.string.audio_path_switch_title)) }
    )
}
