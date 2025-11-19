package com.pixnpunk.stock.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.network.fetchForGet
import com.pixnpunk.stock.domain.repository.RemoteRepository
import com.pixnpunk.utils.FileUtil.writeFileToDir
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.jsoup.Jsoup
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Duration.Companion.minutes

class RemoteRepositoryImpl : RemoteRepository, KoinComponent {

    private val _progressDownloadFlow: MutableSharedFlow<Float> = MutableSharedFlow()
    override val progressDownloadFlow: SharedFlow<Float> = _progressDownloadFlow

    internal companion object {
        const val INITIAL_PAGE_INDEX = 0
        const val PAGE_SIZE = 15
        const val API_QUERY_PAGE = "page"
        const val API_QUERY_PER_PAGE = "per_page"
        const val REMOTE_URL = "https://qsp.org/gamestock/gamestock2.php"
    }

    override suspend fun fetchDescription(descUrl: String): String =
        Jsoup.parse(
            get<HttpClient>().fetchForGet<String>(descUrl).getOrDefault("")
        ).getElementById("sobi2Details_field_description")?.html() ?: ""

    override suspend fun fetchRemoteSource(): Flow<PagingData<GameEntry>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { RemotePagingSource(get()) }
        ).flow

    override suspend fun downloadGameArch(downloadLink: String, destDirPath: String) {
        try {
            get<HttpClient>().prepareGet(
                urlString = downloadLink,
                block = {
                    val timeout = 30.minutes.inWholeMilliseconds
                    timeout {
                        requestTimeoutMillis = timeout
                        connectTimeoutMillis = timeout
                        socketTimeoutMillis = timeout
                    }

                    onDownload { bytesSentTotal, contentLength ->
                        if (contentLength != null) {
                            _progressDownloadFlow.emit(
                                bytesSentTotal.toFloat() / contentLength.toFloat()
                            )
                        }
                    }
                }
            ).execute { response ->
                if (response.status.isSuccess()) {
                    val contentDisposition = response.headers[HttpHeaders.ContentDisposition]
                    val headerValues = parseHeaderValue(contentDisposition)
                    val fileName = headerValues.firstOrNull()?.params?.firstOrNull()?.value
                        ?: "#${(0..Int.MAX_VALUE).random()}Game"

                    writeFileToDir(
                        fileChannel = response.bodyAsChannel(),
                        fileName = fileName,
                        destDirPath = destDirPath
                    )

                    _progressDownloadFlow.emit(1.0f)
                } else {
                    println("Failed to download file. HTTP Status: ${response.status.value}")
                }
            }
        } catch (e: Exception) {
            println("Error: $e")
        }
    }
}
