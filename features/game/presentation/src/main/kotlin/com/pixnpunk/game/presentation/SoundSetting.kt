package com.pixnpunk.game.presentation

import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.domain.repository.AudioPlayerInteractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal fun Flow<SettingsEntity>.applySoundSetting(
    audioPlayer: AudioPlayerInteractor
): Flow<Boolean> =
    map { it.isSoundEnabled }
        .distinctUntilChanged()
        .onEach { isEnabled ->
            audioPlayer.isSoundEnabled = isEnabled
            if (isEnabled) audioPlayer.resumeAll() else audioPlayer.pauseAll()
        }
