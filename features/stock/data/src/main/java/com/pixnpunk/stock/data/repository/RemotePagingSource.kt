package com.pixnpunk.stock.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.data.mapper.toDomain
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_QUERY_PAGE
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.API_QUERY_PER_PAGE
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.INITIAL_PAGE_INDEX
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.PAGE_SIZE
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl.Companion.REMOTE_URL
import com.pixnpunk.stock.domain.model.RemoteGamesList
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class RemotePagingSource(
    private val ktorClient: HttpClient,
) : PagingSource<Int, GameEntry>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GameEntry> {
        return try {
            val currentPage = params.key ?: INITIAL_PAGE_INDEX

            val responseResult = ktorClient.get(REMOTE_URL) {
                parameter(API_QUERY_PAGE, currentPage)
                parameter(API_QUERY_PER_PAGE, PAGE_SIZE)
            }.body<RemoteGamesList>()
            val boxGameEntries = responseResult.gameList.toSet()
            val gameEntries = boxGameEntries.toList().toDomain()

            LoadResult.Page(
                data = gameEntries,
                prevKey = if (currentPage == INITIAL_PAGE_INDEX) null else currentPage - 1,
                nextKey = if (currentPage == responseResult.maxPages) null else currentPage + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, GameEntry>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}