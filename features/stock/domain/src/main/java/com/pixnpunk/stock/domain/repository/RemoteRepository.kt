package com.pixnpunk.stock.domain.repository

import androidx.paging.PagingData
import com.pixnpunk.dto.GameEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface RemoteRepository {
    val progressDownloadFlow: SharedFlow<Float>

    suspend fun fetchDescription(descUrl: String): String
    suspend fun fetchRemoteSource(): Flow<PagingData<GameEntry>>
    suspend fun downloadGameArch(downloadLink: String, destDirPath: String)
}