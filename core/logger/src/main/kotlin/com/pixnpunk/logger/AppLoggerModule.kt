package com.pixnpunk.logger

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appLoggerModule = module {
    singleOf(::AppLoggerImpl) { bind<AppLogger>() }
}
