package com.pixnpunk.natives.di

import com.pixnpunk.natives.NativeViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val supervisorModule = module {
    singleOf(::NativeViewModel)
}