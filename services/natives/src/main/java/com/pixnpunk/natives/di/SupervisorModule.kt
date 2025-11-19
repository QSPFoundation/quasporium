package com.pixnpunk.natives.di

import com.pixnpunk.natives.SupervisorViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val supervisorModule = module {
    singleOf(::SupervisorViewModel)
}