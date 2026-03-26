package com.pixnpunk.quasporium.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.settings.data.repository.SettingsRepository
import com.pixnpunk.stock.domain.usecase.DownloadGameArchUseCase
import com.pixnpunk.stock.domain.usecase.FillAndInsertEntryUseCase
import com.pixnpunk.stock.domain.usecase.UpsertEntryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RootExecutor(
    private val fillAndInsertEntryUseCase: FillAndInsertEntryUseCase,
    private val upsertEntryUseCase: UpsertEntryUseCase,
    private val downloadGameArchUseCase: DownloadGameArchUseCase,
    private val settingsRepository: SettingsRepository
) : CoroutineExecutor<RootStore.Intent, RootStore.Action, RootStore.State, RootStore.Message, RootStore.Label>() {
    override fun executeIntent(intent: RootStore.Intent) {
        when (intent) {
            is RootStore.Intent.AddNewGame -> {
                scope.launch(Dispatchers.IO) { fillAndInsertEntryUseCase.execute(intent.entry) }
            }

            is RootStore.Intent.EditGame -> {
                scope.launch(Dispatchers.IO) { upsertEntryUseCase.execute(intent.entry) }
            }

            is RootStore.Intent.DownloadNewGame -> {
                scope.launch(Dispatchers.IO) {
                    downloadGameArchUseCase.execute(
                        downloadEntry = intent.entry
                    )
                }
            }

            is RootStore.Intent.RestoreColorConfig ->
                settingsRepository.restoreColorValue(intent.state)

            is RootStore.Intent.UpdateColorConfig ->
                settingsRepository.updateColorValue(intent.state, intent.color)

            is RootStore.Intent.UpdateImageConfig ->
                settingsRepository.updateImageSize(intent.state, intent.input.toInt())
        }
    }
}