package com.pixnpunk.notification

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val notificationModule = module {
    singleOf(::NotificationHelper)
}