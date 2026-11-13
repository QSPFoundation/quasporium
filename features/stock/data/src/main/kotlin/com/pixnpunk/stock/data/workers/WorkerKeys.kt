package com.pixnpunk.stock.data.workers

internal object WorkerKeys {
    const val DOWNLOAD_URL = "url"
    const val GAME_DIR_NAME = "gameDirName"
    const val GAME_DATA_JSON = "gameDataJson"

    const val PROGRESS_STEP = "step"
    const val PROGRESS_VALUE = "progress"

    const val INSTALL_TAG = "GameInstall"
    const val GAME_DIR_PREFIX = "Game_"
    const val GAME_DIR_TAG_PREFIX = "gameDir:"

    fun installWorkName(fileUrl: String) = "GameDownload_$fileUrl"
}

internal enum class InstallStep { DOWNLOAD, UNPACK }

internal class UnpackException(cause: Throwable) : Exception(cause.message, cause)
