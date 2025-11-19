package com.pixnpunk.stock.data.mapper

import com.pixnpunk.database.model.GameEntity
import com.pixnpunk.dto.GameEntry

fun GameEntry.toEntity(): GameEntity {
    return GameEntity(
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
        gameDirPath = gameDirPath,
        gameFilesPath = gameFilesPath
    )
}

fun GameEntity.toDomain(): GameEntry {
    return GameEntry(
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
        gameDirPath = gameDirPath,
        gameFilesPath = gameFilesPath
    )
}

fun List<GameEntity>.toDomain(): List<GameEntry> {
    return map { value -> value.toDomain() }
}