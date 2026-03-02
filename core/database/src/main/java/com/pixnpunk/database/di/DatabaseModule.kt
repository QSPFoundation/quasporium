package com.pixnpunk.database.di

import androidx.room.Room
import com.pixnpunk.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = "games.db"
        ).build()
    }
    single { get<AppDatabase>().gameDao() }
}