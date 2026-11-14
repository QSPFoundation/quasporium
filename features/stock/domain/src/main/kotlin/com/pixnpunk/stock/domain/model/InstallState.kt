package com.pixnpunk.stock.domain.model

sealed interface InstallState {
    data object NotStarted : InstallState
    data object Queued : InstallState
    data object Paused : InstallState
    data class Downloading(val progress: Float) : InstallState
    data class Unpacking(val progress: Float) : InstallState
    data object Installed : InstallState
    data object Failed : InstallState
}
