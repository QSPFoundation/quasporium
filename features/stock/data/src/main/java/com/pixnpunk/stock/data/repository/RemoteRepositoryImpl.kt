package com.pixnpunk.stock.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.asString
import com.pixnpunk.stock.data.workers.DownloadGameWorker
import com.pixnpunk.stock.data.workers.UnpackGameWorker
import com.pixnpunk.stock.domain.repository.RemoteRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RemoteRepositoryImpl : RemoteRepository, KoinComponent {

    internal companion object {
        const val INITIAL_PAGE_INDEX = 1
        const val PAGE_SIZE = 15
        const val API_QUERY_PAGE = "page"
        const val API_QUERY_PER_PAGE = "per_page"
        const val REMOTE_URL = "https://qsp.org/api/v1/games"
    }

    override suspend fun fetchRemoteSource(): Flow<PagingData<GameEntry>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { RemotePagingSource(get()) }
        ).flow

    override suspend fun downloadGame(downloadEntry: GameEntry) {
        val downloadRequest = OneTimeWorkRequestBuilder<DownloadGameWorker>()
            .setInputData(
                workDataOf(
                    "url" to downloadEntry.fileUrl,
                    "destDir" to downloadEntry.gameDirPath
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        val unpackRequest = OneTimeWorkRequestBuilder<UnpackGameWorker>()
            .setInputData(
                workDataOf(
                    "gameDataJson" to downloadEntry.asString()
                )
            )
            .build()

        get<WorkManager>()
            .beginUniqueWork(
                "GameDownload_${downloadEntry.fileUrl}",
                ExistingWorkPolicy.KEEP,
                downloadRequest
            )
            .then(unpackRequest)
            .enqueue()
    }
}
