package com.pixnpunk.stock.data.mapper

import com.pixnpunk.database.model.TranslatorEntity
import com.pixnpunk.stock.data.remote.NameCountItemRemote
import com.pixnpunk.stock.domain.model.TranslatorEntry

fun NameCountItemRemote.toTranslatorEntity(): TranslatorEntity =
    TranslatorEntity(
        name = this.name,
        gamesCount = this.gamesCount
    )

fun TranslatorEntity.toDomain(): TranslatorEntry =
    TranslatorEntry(
        name = this.name,
        gamesCount = this.gamesCount
    )
