package com.pixnpunk.stock.data.repository

import android.content.Context
import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pixnpunk.common.runCatchingCancellable
import com.pixnpunk.database.AppDatabase
import com.pixnpunk.database.dao.AuthorDao
import com.pixnpunk.database.dao.TranslatorDao
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.stock.data.mapper.toAuthorEntity
import com.pixnpunk.stock.data.mapper.toDomain
import com.pixnpunk.stock.data.mapper.toTranslatorEntity
import com.pixnpunk.stock.data.remote.GenericApiResponse
import com.pixnpunk.stock.data.remote.NameCountItemRemote
import com.pixnpunk.stock.data.workers.InstallGameWorker
import com.pixnpunk.stock.data.workers.WorkerKeys.DOWNLOAD_URL
import com.pixnpunk.stock.data.workers.WorkerKeys.GAME_DATA_JSON
import com.pixnpunk.stock.data.workers.WorkerKeys.GAME_DIR_NAME
import com.pixnpunk.stock.data.workers.WorkerKeys.GAME_DIR_PREFIX
import com.pixnpunk.stock.data.workers.WorkerKeys.GAME_DIR_TAG_PREFIX
import com.pixnpunk.stock.data.workers.WorkerKeys.INSTALL_TAG
import com.pixnpunk.stock.data.workers.WorkerKeys.installWorkName
import com.pixnpunk.stock.domain.model.AuthorEntry
import com.pixnpunk.stock.domain.model.SearchState
import com.pixnpunk.stock.domain.model.TranslatorEntry
import com.pixnpunk.stock.domain.repository.RemoteRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID
import java.util.concurrent.TimeUnit

class RemoteRepositoryImpl(
    private val context: Context,
    private val ktorClient: Lazy<HttpClient>,
    private val workManager: WorkManager,
    private val appDatabase: AppDatabase,
    private val authorDao: AuthorDao,
    private val translatorDao: TranslatorDao
) : RemoteRepository {

    internal companion object {
        const val INITIAL_PAGE_INDEX = 1
        const val PAGE_SIZE = 15
        const val API_QUERY_PAGE = "page"
        const val API_QUERY_PER_PAGE = "per_page"
        const val API_SEARCH_AUTHORS = "search-authors"
        const val API_SEARCH_TRANSLATORS = "search-translators"
        const val API_SEARCH_DESC = "search-description"
        const val API_SEARCH_NAME = "search-name"
        const val API_QUERY_SORT = "sort"
        const val API_QUERY_ORDER = "order"
        const val API_QUERY_LANG = "language"
        const val DOWNLOAD_BACKOFF_SECONDS = 10L
        const val REMOTE_GAMES_URL = "https://qsp.org/api/v1/games"
        const val REMOTE_AUTHORS_URL = "$REMOTE_GAMES_URL/authors"
        const val REMOTE_TRANSLATORS_URL = "$REMOTE_GAMES_URL/translators"
    }

    override fun getGameEntriesPager(
        searchState: SearchState
    ): Flow<PagingData<GameEntry>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE * 2,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                RemotePagingSource(
                    ktorClient.value,
                    searchState.query,
                    searchState.sortBy,
                    searchState.sortOrder,
                    searchState.selectedLang,
                    searchState.activeTokens
                )
            }
        ).flow

    override fun getAuthorSuggestions(query: String): Flow<List<AuthorEntry>> =
        if (query.isEmpty()) {
            authorDao.getRandomAuthors().map { list -> list.map { it.toDomain() } }
        } else {
            authorDao.searchAuthorsLocal(query).map { list -> list.map { it.toDomain() } }
        }

    override fun getTranslatorSuggestions(query: String): Flow<List<TranslatorEntry>> =
        if (query.isEmpty()) {
            translatorDao.getRandomTranslators().map { list -> list.map { it.toDomain() } }
        } else {
            translatorDao.searchTranslatorLocal(query).map { list -> list.map { it.toDomain() } }
        }

    override suspend fun downloadGame(downloadEntry: GameEntry) {
        val gameDirName = GAME_DIR_PREFIX + UUID.randomUUID().toString().replace("-", "")

        val installRequest =
            OneTimeWorkRequestBuilder<InstallGameWorker>()
                .setInputData(
                    workDataOf(
                        DOWNLOAD_URL to downloadEntry.fileUrl,
                        GAME_DIR_NAME to gameDirName,
                        GAME_DATA_JSON to Json.encodeToString(downloadEntry)
                    )
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    DOWNLOAD_BACKOFF_SECONDS,
                    TimeUnit.SECONDS
                )
                .addTag(INSTALL_TAG)
                .addTag(GAME_DIR_TAG_PREFIX + gameDirName)
                .build()

        workManager.enqueueUniqueWork(
            installWorkName(downloadEntry.fileUrl),
            ExistingWorkPolicy.KEEP,
            installRequest
        )
    }

    override suspend fun removeAbandonedInstalls() =
        withContext(Dispatchers.IO) {
            val gameDirs = context.filesDir
                .listFiles { file ->
                    file.isDirectory && file.name.startsWith(GAME_DIR_PREFIX)
                }
                .orEmpty()
            if (gameDirs.isEmpty()) return@withContext

            val installing =
                workManager.getWorkInfosByTagFlow(INSTALL_TAG).first()
                    .filterNot { it.state.isFinished }
                    .flatMap { it.tags }
                    .filter { it.startsWith(GAME_DIR_TAG_PREFIX) }
                    .map { it.removePrefix(GAME_DIR_TAG_PREFIX) }
                    .toSet()
            val installed =
                appDatabase.gameDao().getAll()
                    .flatMap { it.gameDirPath.toUri().pathSegments }
                    .toSet()

            gameDirs
                .filter { it.name !in installing && it.name !in installed }
                .forEach { it.deleteRecursively() }
        }

    override suspend fun syncDictionaries() =
        withContext(Dispatchers.IO) {
            runCatchingCancellable {
                coroutineScope {
                    val allAuthors = async { fetchAllPages(REMOTE_AUTHORS_URL) }.await()
                    val allTranslators = async { fetchAllPages(REMOTE_TRANSLATORS_URL) }.await()
                    appDatabase.withTransaction {
                        authorDao.clearAll()
                        authorDao.insertAll(allAuthors.map { it.toAuthorEntity() })

                        translatorDao.clearAll()
                        translatorDao.insertAll(allTranslators.map { it.toTranslatorEntity() })
                    }
                }
            }
        }

    private suspend fun fetchAllPages(url: String): List<NameCountItemRemote> {
        val allItems = mutableListOf<NameCountItemRemote>()
        var currentPage = 1
        var hasNextPage = true

        while (hasNextPage) {
            val response = ktorClient.value.get(url) {
                parameter(API_QUERY_PAGE, currentPage)
                parameter(API_QUERY_PER_PAGE, 100)
            }.body<GenericApiResponse<NameCountItemRemote>>()

            allItems.addAll(response.data)

            if (response.meta.currentPage >= response.meta.lastPage) {
                hasNextPage = false
            } else {
                currentPage++
            }
        }

        return allItems
    }
}
