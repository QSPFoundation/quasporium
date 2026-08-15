package com.pixnpunk.stock.data.mapper

import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.GameListType
import com.pixnpunk.stock.domain.model.RemoteGameData

fun List<RemoteGameData>.toDomain(): List<GameEntry> =
    map { remoteGameData ->
        GameEntry(
            id = remoteGameData.id,
            listId = GameListType.Downloadable,
            author = remoteGameData.authors,
            portedBy = remoteGameData.translators ?: "",
            version = remoteGameData.ver,
            title = remoteGameData.name,
            lang = remoteGameData.lang ?: "",
            gameIconPath = remoteGameData.iconUrl ?: "",
            fileUrl = remoteGameData.fileUrl,
            fileSize = remoteGameData.fileSize ?: 0,
            descUrl = remoteGameData.descriptionHtml,
            pubDate = remoteGameData.createdAt,
            modDate = remoteGameData.updatedAt
        )
    }