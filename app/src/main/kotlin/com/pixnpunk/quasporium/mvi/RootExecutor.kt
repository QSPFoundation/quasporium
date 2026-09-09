package com.pixnpunk.quasporium.mvi

import androidx.compose.ui.graphics.toArgb
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.settings.dto.usecase.RestoreColorValueUseCase
import com.pixnpunk.settings.dto.usecase.UpdateColorUseCase
import com.pixnpunk.settings.dto.usecase.UpdateCustomFontPathUseCase
import com.pixnpunk.settings.dto.usecase.UpdateImageSizeUseCase
import com.pixnpunk.stock.domain.usecase.DownloadGameArchUseCase
import com.pixnpunk.stock.domain.usecase.FillAndInsertEntryUseCase
import com.pixnpunk.stock.domain.usecase.MigrateIfNeededUseCase
import com.pixnpunk.stock.domain.usecase.UpsertEntryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RootExecutor(
    private val fillAndInsertEntryUseCase: FillAndInsertEntryUseCase,
    private val upsertEntryUseCase: UpsertEntryUseCase,
    private val downloadGameArchUseCase: DownloadGameArchUseCase,
    private val updateColorUseCase: UpdateColorUseCase,
    private val restoreColorValueUseCase: RestoreColorValueUseCase,
    private val updateImageSizeUseCase: UpdateImageSizeUseCase,
    private val updateCustomFontPathUseCase: UpdateCustomFontPathUseCase,
    private val migrateIfNeededUseCase: MigrateIfNeededUseCase
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
                scope.launch { restoreColorValueUseCase(intent.state) }

            is RootStore.Intent.UpdateColorConfig ->
                scope.launch { updateColorUseCase(intent.state, intent.color.toArgb()) }

            is RootStore.Intent.UpdateImageConfig ->
                scope.launch { updateImageSizeUseCase(intent.state, intent.input.toInt()) }

            is RootStore.Intent.UpdateCustomFontPath ->
                scope.launch { updateCustomFontPathUseCase(intent.path) }
        }
    }

    override fun executeAction(action: RootStore.Action) {
        when (action) {
            RootStore.Action.MigrateIfNeeded ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        migrateIfNeededUseCase.execute()
                    }

                    dispatch(RootStore.Message.MigrationComplete)
                }
        }
    }
}
