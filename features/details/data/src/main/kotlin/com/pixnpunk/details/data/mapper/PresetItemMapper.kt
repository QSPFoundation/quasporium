package com.pixnpunk.details.data.mapper

import com.pixnpunk.database.model.PresetEntity
import com.pixnpunk.details.domain.model.PresetItem

fun PresetEntity.toDomain(): PresetItem =
    PresetItem(
        id = id,
        name = presetName,
        timestamp = timestamp,
        lastPlayedFile = lastPlayedFile,
        accentColor = accentColor,
        backImagePath = backImagePath
    )

fun List<PresetEntity>.toDomain(): List<PresetItem> = map { it.toDomain() }
