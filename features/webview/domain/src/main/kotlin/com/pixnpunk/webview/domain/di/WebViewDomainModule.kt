package com.pixnpunk.webview.domain.di

import com.pixnpunk.webview.domain.usecase.FormatGameHtmlUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val webViewDomainModule = module {
    factoryOf(::FormatGameHtmlUseCase)
}
