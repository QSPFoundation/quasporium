package com.pixnpunk.stock.domain.di

import com.pixnpunk.stock.domain.usecase.DeleteLocalGameEntries
import com.pixnpunk.stock.domain.usecase.DownloadGameArchUseCase
import com.pixnpunk.stock.domain.usecase.FetchLocalSourceUseCase
import com.pixnpunk.stock.domain.usecase.FetchRemoteGamesExcludingLocalUseCase
import com.pixnpunk.stock.domain.usecase.FetchRemoteSourceUseCase
import com.pixnpunk.stock.domain.usecase.FillAndInsertEntryUseCase
import com.pixnpunk.stock.domain.usecase.ObserveEntryByIdUseCase
import com.pixnpunk.stock.domain.usecase.SearchLocalGameEntryUseCase
import com.pixnpunk.stock.domain.usecase.SearchRemoteGameEntryUseCase
import com.pixnpunk.stock.domain.usecase.UpsertEntryUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    singleOf(::FillAndInsertEntryUseCase)
    singleOf(::FetchRemoteSourceUseCase)
    singleOf(::FetchRemoteGamesExcludingLocalUseCase)
    singleOf(::FetchLocalSourceUseCase)
    singleOf(::DeleteLocalGameEntries)
    singleOf(::DownloadGameArchUseCase)
    singleOf(::ObserveEntryByIdUseCase)
    singleOf(::SearchLocalGameEntryUseCase)
    singleOf(::SearchRemoteGameEntryUseCase)
    singleOf(::UpsertEntryUseCase)
}