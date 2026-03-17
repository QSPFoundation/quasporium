package com.pixnpunk.stock.domain.di

import com.pixnpunk.stock.domain.usecase.DeleteLocalGameEntriesUseCase
import com.pixnpunk.stock.domain.usecase.DownloadGameArchUseCase
import com.pixnpunk.stock.domain.usecase.FillAndInsertEntryUseCase
import com.pixnpunk.stock.domain.usecase.GetAuthorSuggestionUseCase
import com.pixnpunk.stock.domain.usecase.GetLocalGameEntriesUseCase
import com.pixnpunk.stock.domain.usecase.GetRemoteGameEntriesExcludingLocalUseCase
import com.pixnpunk.stock.domain.usecase.GetTranslatorSuggestionUseCase
import com.pixnpunk.stock.domain.usecase.ObserveEntryByIdUseCase
import com.pixnpunk.stock.domain.usecase.SyncDictionariesUseCase
import com.pixnpunk.stock.domain.usecase.UpsertEntryUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    singleOf(::FillAndInsertEntryUseCase)
    singleOf(::DeleteLocalGameEntriesUseCase)
    singleOf(::DownloadGameArchUseCase)
    singleOf(::ObserveEntryByIdUseCase)
    singleOf(::UpsertEntryUseCase)
    singleOf(::SyncDictionariesUseCase)
    singleOf(::GetLocalGameEntriesUseCase)
    singleOf(::GetRemoteGameEntriesExcludingLocalUseCase)
    singleOf(::GetAuthorSuggestionUseCase)
    singleOf(::GetTranslatorSuggestionUseCase)
}