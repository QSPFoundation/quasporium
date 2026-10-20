package com.pixnpunk.game.presentation

import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal fun <T : Any> Flow<SettingsEntity>.customFont(load: (String) -> T?): Flow<T?> =
    map { settings -> settings.customFontPath.takeIf { settings.isUseCustomFont && it.isNotBlank() } }
        .distinctUntilChanged()
        .map { path -> path?.let(load) }
