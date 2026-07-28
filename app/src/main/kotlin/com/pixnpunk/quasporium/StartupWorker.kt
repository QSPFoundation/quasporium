package com.pixnpunk.quasporium

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pixnpunk.stock.domain.usecase.SyncDictionariesUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class StartupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    override suspend fun doWork(): Result {
        get<SyncDictionariesUseCase>().execute()
        return Result.success()
    }
}
