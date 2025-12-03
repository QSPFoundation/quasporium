package com.pixnpunk.stock.domain.repository

import androidx.paging.PagingData
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.ProgressState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface RemoteRepository {
    val progressDownloadFlow: SharedFlow<ProgressState>

    suspend fun fetchDescription(descUrl: String): String
    suspend fun fetchRemoteSource(): Flow<PagingData<GameEntry>>
    suspend fun downloadUnpackGame(downloadLink: String, destDirPath: String): Result<String>
}