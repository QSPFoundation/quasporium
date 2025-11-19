package com.pixnpunk.database.di

import com.pixnpunk.database.AppDatabase
import com.pixnpunk.database.dao.GameDao
import org.koin.dsl.module

val databaseModule = module {
    single<GameDao> { AppDatabase.getInstance(get()).gameDao() }
}