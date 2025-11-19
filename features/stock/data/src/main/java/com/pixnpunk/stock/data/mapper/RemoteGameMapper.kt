package com.pixnpunk.stock.data.mapper

import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.GameListType
import com.pixnpunk.stock.domain.model.RemoteGameData

fun List<RemoteGameData>.toDomain(): List<GameEntry> {
    return map { remoteGameData ->
        GameEntry(
            id = remoteGameData.id,
            listId = GameListType.Downloadable,
            author = remoteGameData.author,
            portedBy = remoteGameData.portedBy,
            version = remoteGameData.version,
            title = remoteGameData.title,
            lang = remoteGameData.lang,
            player = remoteGameData.player,
            gameIconPath = remoteGameData.icon,
            fileUrl = remoteGameData.fileUrl,
            fileSize = remoteGameData.fileSize,
            fileExt = remoteGameData.fileExt,
            descUrl = remoteGameData.descUrl,
            pubDate = remoteGameData.pubDate,
            modDate = remoteGameData.modDate
        )
    }
}