package com.pixnpunk.stock.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.data.mapper.toDomain
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_QUERY_LANG
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_QUERY_ORDER
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_QUERY_PAGE
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_QUERY_PER_PAGE
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_QUERY_SORT
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_SEARCH_AUTHORS
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_SEARCH_DESC
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_SEARCH_NAME
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_SEARCH_TRANSLATORS
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.INITIAL_PAGE_INDEX
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.PAGE_SIZE
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.REMOTE_GAMES_URL
import com.pixnpunk.stock.domain.model.RemoteGamesList
import com.pixnpunk.stock.domain.model.SearchToken
import com.pixnpunk.stock.domain.model.SearchTokenType
import com.pixnpunk.stock.domain.model.SortBy
import com.pixnpunk.stock.domain.model.SortOrder
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class RemotePagingSource(
    private val ktorClient: HttpClient,
    private val searchQuery: String = "",
    private val sortBy: SortBy,
    private val sortOrder: SortOrder,
    private val language: String? = null,
    private val activeTokens: List<SearchToken> = emptyList()
) : PagingSource<Int, GameEntry>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GameEntry> =
        try {
            val currentPage = params.key ?: INITIAL_PAGE_INDEX

            val responseResult = ktorClient.get(REMOTE_GAMES_URL) {
                parameter(API_QUERY_PAGE, currentPage)
                parameter(API_QUERY_PER_PAGE, PAGE_SIZE)
                parameter(API_SEARCH_NAME, searchQuery)
                if (activeTokens.isNotEmpty()) {
                    activeTokens.forEach {
                        when (it.type) {
                            SearchTokenType.AUTHOR -> parameter(API_SEARCH_AUTHORS, it.value)
                            SearchTokenType.TRANSLATOR -> parameter(API_SEARCH_TRANSLATORS, it.value)
                            SearchTokenType.TITLE -> parameter(API_SEARCH_NAME, it.value)
                            SearchTokenType.DESCRIPTION -> parameter(API_SEARCH_DESC, it.value)
                        }
                    }
                }
                parameter(API_QUERY_SORT, sortBy.apiName)
                parameter(API_QUERY_ORDER, sortOrder.apiName)
                parameter(API_QUERY_LANG, language)
            }.body<RemoteGamesList>()
            val boxGameEntries = responseResult.data.toSet()
            val gameEntries = boxGameEntries.toList().toDomain()

            LoadResult.Page(
                data = gameEntries,
                prevKey = if (currentPage == INITIAL_PAGE_INDEX) null else currentPage - 1,
                nextKey =
                    if (searchQuery.isNotEmpty() || activeTokens.isNotEmpty()) {
                        if (currentPage == responseResult.meta.lastPage) null else currentPage + 1
                    } else {
                        if (currentPage == responseResult.meta.total) null else currentPage + 1
                    }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    override fun getRefreshKey(state: PagingState<Int, GameEntry>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}
