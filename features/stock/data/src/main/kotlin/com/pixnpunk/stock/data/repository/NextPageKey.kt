package com.pixnpunk.stock.data.repository

internal fun nextPageKey(currentPage: Int, lastPage: Int, isPageEmpty: Boolean): Int? =
    if (currentPage >= lastPage || isPageEmpty) null else currentPage + 1
