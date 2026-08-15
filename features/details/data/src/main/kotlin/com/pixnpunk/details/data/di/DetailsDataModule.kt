package com.pixnpunk.details.data.di

import com.pixnpunk.details.data.repository.ProfileRepositoryImpl
import com.pixnpunk.details.data.repository.SavesRepositoryImpl
import com.pixnpunk.details.domain.repository.ProfileRepository
import com.pixnpunk.details.domain.repository.SavesRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val detailsDataModule = module {
    singleOf(::ProfileRepositoryImpl) { bind<ProfileRepository>() }
    singleOf(::SavesRepositoryImpl) { bind<SavesRepository>() }
}
