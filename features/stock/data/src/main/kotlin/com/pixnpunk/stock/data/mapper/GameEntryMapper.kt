package com.pixnpunk.stock.data.mapper

import com.pixnpunk.database.model.GameEntity
import com.pixnpunk.dto.GameEntry

fun GameEntry.toEntity(): GameEntity =
    GameEntity(
        id = id,
        listId = listId,
        author = author,
        portedBy = portedBy,
        version = version,
        title = title,
        lang = lang,
        player = player,
        gameIconPath = gameIconPath,
        fileUrl = fileUrl,
        fileSize = fileSize,
        fileExt = fileExt,
        descUrl = descUrl,
        pubDate = pubDate,
        modDate = modDate,
        gameDirPath = gameDirPath
    )

fun GameEntity.toDomain(): GameEntry =
    GameEntry(
        id = id,
        listId = listId,
        author = author,
        portedBy = portedBy,
        version = version,
        title = title,
        lang = lang,
        player = player,
        gameIconPath = gameIconPath,
        fileUrl = fileUrl,
        fileSize = fileSize,
        fileExt = fileExt,
        descUrl = descUrl,
        pubDate = pubDate,
        modDate = modDate,
        gameDirPath = gameDirPath
    )

fun List<GameEntity>.toDomain(): List<GameEntry> = map { value -> value.toDomain() }