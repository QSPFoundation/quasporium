package com.pixnpunk.stock.data.di

import androidx.work.WorkManager
import com.pixnpunk.stock.data.download.GameDownloader
import com.pixnpunk.stock.data.repository.LocalRepositoryImpl
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl
import com.pixnpunk.stock.data.workers.InstallGameWorker
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.stock.domain.repository.RemoteRepository
import io.ktor.client.*
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val stockDataModule = module {
    single { WorkManager.getInstance(get()) }
    factory { GameDownloader(get()) }
    workerOf(::InstallGameWorker)

    singleOf(::LocalRepositoryImpl) {
        bind<LocalRepository>()
    }

    single<RemoteRepository> {
        RemoteRepositoryImpl(
            get(), inject<HttpClient>(), get(),
            get(), get()
        )
    }
}
