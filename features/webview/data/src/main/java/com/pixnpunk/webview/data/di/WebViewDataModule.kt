package com.pixnpunk.webview.data.di

import com.pixnpunk.webview.data.repository.HtmlProcessorRepositoryImpl
import com.pixnpunk.webview.domain.repository.HtmlProcessorRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val webViewDataModule = module {
    singleOf(::HtmlProcessorRepositoryImpl) {
        bind<HtmlProcessorRepository>()
    }
}