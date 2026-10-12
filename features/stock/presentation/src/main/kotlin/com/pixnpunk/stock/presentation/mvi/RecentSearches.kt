package com.pixnpunk.stock.presentation.mvi

import com.pixnpunk.stock.presentation.model.SearchTokenUI

internal const val MAX_RECENT_SEARCHES = 10

internal fun List<SearchTokenUI>.withRecentSearch(token: SearchTokenUI): List<SearchTokenUI> =
    (filter { it != token } + token).takeLast(MAX_RECENT_SEARCHES)
