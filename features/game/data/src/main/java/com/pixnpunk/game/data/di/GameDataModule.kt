package com.pixnpunk.game.data.di

import com.pixnpunk.game.data.NativeViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val gameDataModule = module {
    singleOf(::NativeViewModel)
}