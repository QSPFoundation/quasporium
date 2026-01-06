package com.pixnpunk.quasporium.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.stock.domain.usecase.DownloadGameArchUseCase
import com.pixnpunk.stock.domain.usecase.FillAndInsertEntryUseCase
import com.pixnpunk.stock.domain.usecase.UpsertEntryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RootExecutor(
    private val fillAndInsertEntryUseCase: FillAndInsertEntryUseCase,
    private val upsertEntryUseCase: UpsertEntryUseCase,
    private val downloadGameArchUseCase: DownloadGameArchUseCase,
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
                        downloadLink = intent.entry.fileUrl,
                        destDirPath = intent.entry.gameDirPath
                    ).onSuccess {
                        fillAndInsertEntryUseCase.execute(intent.entry.copy(gameDirPath = it))
                    }
                }
            }

            is RootStore.Intent.UpdateColorConfig -> {
                publish(RootStore.Label.ColorConfig(intent.state, intent.color))
            }

            is RootStore.Intent.UpdateImageConfig -> {
                publish(RootStore.Label.ImageConfig(intent.state, intent.input))
            }
        }
    }
}