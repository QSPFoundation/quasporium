package com.pixnpunk.quasporium.logger

import com.pixnpunk.logger.AppLogger
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val distributionLoggerModule = module {
    singleOf(::FirebaseAppLogger) { bind<AppLogger>() }
}
