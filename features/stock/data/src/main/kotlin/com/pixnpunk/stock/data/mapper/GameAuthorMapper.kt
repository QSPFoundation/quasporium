package com.pixnpunk.stock.data.mapper

import com.pixnpunk.database.model.AuthorEntity
import com.pixnpunk.stock.data.remote.NameCountItemRemote
import com.pixnpunk.stock.domain.model.AuthorEntry

fun NameCountItemRemote.toAuthorEntity(): AuthorEntity =
    AuthorEntity(
        name = this.name,
        gamesCount = this.gamesCount
    )

fun AuthorEntity.toDomain(): AuthorEntry =
    AuthorEntry(
        name = this.name,
        gamesCount = this.gamesCount
    )
