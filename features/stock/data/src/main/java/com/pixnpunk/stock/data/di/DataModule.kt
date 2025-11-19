package com.pixnpunk.stock.data.di

import com.pixnpunk.stock.data.repository.LocalRepositoryImpl
import com.pixnpunk.stock.data.repository.RemoteRepositoryImpl
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.stock.domain.repository.RemoteRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    singleOf(::LocalRepositoryImpl) {
        bind<LocalRepository>()
    }
    singleOf(::RemoteRepositoryImpl) {
        bind<RemoteRepository>()
    }
}