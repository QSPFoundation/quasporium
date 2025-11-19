package com.pixnpunk.quasporium

import android.app.Application
import com.pixnpunk.audio.audioModule
import com.pixnpunk.database.di.databaseModule
import com.pixnpunk.natives.di.supervisorModule
import com.pixnpunk.network.networkModule
import com.pixnpunk.settings.settingsModule
import com.pixnpunk.stock.presentation.di.featureStockModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Quasporium : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Quasporium)
            modules(databaseModule, networkModule, settingsModule)
            modules(audioModule, supervisorModule)
            modules(featureStockModules)
        }
    }
}