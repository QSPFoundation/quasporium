package com.pixnpunk.quasporium.mvi

import androidx.compose.ui.graphics.toArgb
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.logger.executorContext
import com.pixnpunk.settings.domain.usecase.RestoreColorValueUseCase
import com.pixnpunk.settings.domain.usecase.UpdateColorUseCase
import com.pixnpunk.settings.domain.usecase.UpdateCustomFontPathUseCase
import com.pixnpunk.settings.domain.usecase.UpdateImageSizeUseCase
import com.pixnpunk.stock.domain.usecase.DownloadGameArchUseCase
import com.pixnpunk.stock.domain.usecase.FillAndInsertEntryUseCase
import com.pixnpunk.stock.domain.usecase.MigrateIfNeededUseCase
import com.pixnpunk.stock.domain.usecase.UpsertEntryUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.CoroutineContext

class RootExecutor(
    private val fillAndInsertEntryUseCase: FillAndInsertEntryUseCase,
    private val upsertEntryUseCase: UpsertEntryUseCase,
    private val downloadGameArchUseCase: DownloadGameArchUseCase,
    private val updateColorUseCase: UpdateColorUseCase,
    private val restoreColorValueUseCase: RestoreColorValueUseCase,
    private val updateImageSizeUseCase: UpdateImageSizeUseCase,
    private val updateCustomFontPathUseCase: UpdateCustomFontPathUseCase,
    private val migrateIfNeededUseCase: MigrateIfNeededUseCase,
    logger: AppLogger,
    mainContext: CoroutineContext = logger.executorContext("RootExecutor"),
    private val ioContext: CoroutineContext = Dispatchers.IO
) : CoroutineExecutor<RootStore.Intent, RootStore.Action, RootStore.State, RootStore.Message, RootStore.Label>(
    mainContext
) {

    override fun executeIntent(intent: RootStore.Intent) {
        when (intent) {
            is RootStore.Intent.AddNewGame -> {
                scope.launch(ioContext) { fillAndInsertEntryUseCase.execute(intent.entry) }
            }

            is RootStore.Intent.EditGame -> {
                scope.launch(ioContext) { upsertEntryUseCase.execute(intent.entry) }
            }

            is RootStore.Intent.DownloadNewGame -> {
                scope.launch(ioContext) {
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
                scope.launch {
                    updateCustomFontPathUseCase(intent.path)
                    intent.replacedPath?.let { withContext(ioContext) { File(it).delete() } }
                }
        }
    }

    override fun executeAction(action: RootStore.Action) {
        when (action) {
            RootStore.Action.MigrateIfNeeded ->
                scope.launch {
                    try {
                        withContext(ioContext) {
                            migrateIfNeededUseCase.execute()
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                    }

                    dispatch(RootStore.Message.MigrationComplete)
                }
        }
    }
}
