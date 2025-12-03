package com.pixnpunk.dto

data class ProgressState(
    val progress: Float = 0F,
    val status: DownloadStatus = DownloadStatus.NOT_STARTED,
    val time: String = ""
)
