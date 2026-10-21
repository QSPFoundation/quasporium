package com.pixnpunk.storage.di

import com.pixnpunk.storage.ArchiveExtractor
import com.pixnpunk.storage.GameFileStorage
import com.pixnpunk.storage.SafGameFileStorage
import com.pixnpunk.storage.SevenZipArchiveExtractor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {
    single<GameFileStorage> { SafGameFileStorage() }
    single<ArchiveExtractor> { SevenZipArchiveExtractor(androidContext()) }
}
