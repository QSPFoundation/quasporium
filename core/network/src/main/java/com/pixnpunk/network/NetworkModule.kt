package com.pixnpunk.network

import io.ktor.client.*
import org.koin.dsl.module

val networkModule get() = module {
    single<HttpClient> { createKtorClient() }
}