package com.pixnpunk.details.data.di

import com.pixnpunk.details.data.repository.PresetRepositoryImpl
import com.pixnpunk.details.data.repository.SavesRepositoryImpl
import com.pixnpunk.details.domain.repository.PresetRepository
import com.pixnpunk.details.domain.repository.SavesRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val detailsDataModule = module {
    singleOf(::PresetRepositoryImpl) { bind<PresetRepository>() }
    singleOf(::SavesRepositoryImpl) { bind<SavesRepository>() }
}
