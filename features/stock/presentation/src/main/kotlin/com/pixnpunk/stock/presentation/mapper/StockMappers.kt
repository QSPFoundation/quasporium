package com.pixnpunk.stock.presentation.mapper

import com.pixnpunk.stock.domain.model.SearchToken
import com.pixnpunk.stock.presentation.model.SearchTokenUI

fun SearchTokenUI.toDomain(): SearchToken =
    SearchToken(value = value, type = type, displayText = displayText)

fun List<SearchTokenUI>.toDomain(): List<SearchToken> = map { it.toDomain() }

fun SearchToken.toUI(): SearchTokenUI =
    SearchTokenUI(value = value, type = type, displayText = displayText)

fun List<SearchToken>.toUI(): List<SearchTokenUI> = map { it.toUI() }
