package com.pixnpunk.stock.data.di

import androidx.work.WorkManager
import com.pixnpunk.stock.data.workers.DownloadGameWorker
import com.pixnpunk.stock.data.repository.LocalRepositoryImpl
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl
import com.pixnpunk.stock.data.workers.UnpackGameWorker
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.stock.domain.repository.RemoteRepository
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    single { WorkManager.getInstance(get()) }
    workerOf(::DownloadGameWorker)
    workerOf(::UnpackGameWorker)
    singleOf(::LocalRepositoryImpl) {
        bind<LocalRepository>()
    }
    singleOf(::RemoteRepositoryImpl) {
        bind<RemoteRepository>()
    }
}