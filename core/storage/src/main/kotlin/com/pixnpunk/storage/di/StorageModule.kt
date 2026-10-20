package com.pixnpunk.storage.di

import com.pixnpunk.storage.GameFileStorage
import com.pixnpunk.storage.SafGameFileStorage
import org.koin.dsl.module

val storageModule = module {
    single<GameFileStorage> { SafGameFileStorage() }
}
