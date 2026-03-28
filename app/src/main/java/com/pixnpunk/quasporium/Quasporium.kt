package com.pixnpunk.quasporium

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pixnpunk.audio.audioModule
import com.pixnpunk.database.di.databaseModule
import com.pixnpunk.game.presentation.di.featureGameModule
import com.pixnpunk.logger.appLoggerModule
import com.pixnpunk.network.networkModule
import com.pixnpunk.notification.notificationModule
import com.pixnpunk.settings.presentation.di.featureSettingsModule
import com.pixnpunk.stock.presentation.di.featureStockModules
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class Quasporium : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Quasporium)
            workManagerFactory()
            modules(
                appLoggerModule, databaseModule,
                networkModule, audioModule,
                notificationModule
            )
            modules(featureGameModule)
            modules(featureSettingsModule)
            modules(featureStockModules)
        }

        WorkManager.getInstance(this).enqueueUniqueWork(
            "AppStartupWork",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<StartupWorker>().build()
        )
    }
}