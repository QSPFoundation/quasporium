package com.pixnpunk.quasporium

import android.app.Application
import android.os.StrictMode
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pixnpunk.database.di.databaseModule
import com.pixnpunk.details.data.di.detailsDataModule
import com.pixnpunk.game.data.di.gameDataModule
import com.pixnpunk.network.networkModule
import com.pixnpunk.notification.notificationModule
import com.pixnpunk.quasporium.di.presentationModule
import com.pixnpunk.quasporium.di.settingsDomainModule
import com.pixnpunk.quasporium.di.stockDomainModule
import com.pixnpunk.quasporium.di.webViewDomainModule
import com.pixnpunk.quasporium.logger.distributionLoggerModule
import com.pixnpunk.settings.data.di.settingsDataModule
import com.pixnpunk.stock.data.di.stockDataModule
import com.pixnpunk.storage.di.storageModule
import com.pixnpunk.webview.data.di.webViewDataModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class Quasporium : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }

        startKoin {
            androidContext(this@Quasporium)
            workManagerFactory()
            modules(
                distributionLoggerModule, databaseModule,
                networkModule, notificationModule, storageModule
            )
            modules(
                detailsDataModule, gameDataModule, settingsDataModule,
                stockDataModule, webViewDataModule
            )
            modules(settingsDomainModule, stockDomainModule, webViewDomainModule)
            modules(presentationModule)
        }

        WorkManager.getInstance(this).enqueueUniqueWork(
            "AppStartupWork",
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<StartupWorker>().build()
        )
    }
}
