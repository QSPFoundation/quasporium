package com.pixnpunk.stock.data.download

import kotlinx.serialization.Serializable

@Serializable
internal data class DownloadState(
    val url: String,
    val fileName: String,
    val totalSize: Long? = null,
    val etag: String? = null,
    val lastModified: String? = null
) {
    companion object {
        const val FILE_NAME = ".download"
    }

    val validator: String?
        get() = etag?.takeUnless { it.startsWith("W/") } ?: lastModified
}
