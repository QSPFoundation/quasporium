package com.pixnpunk.quasporium.di

import com.pixnpunk.details.presentation.mvi.DetailsExecutor
import com.pixnpunk.quasporium.mvi.RootExecutor
import com.pixnpunk.stock.presentation.mvi.StockExecutor
import org.koin.dsl.module

val presentationModule = module {
    factory { RootExecutor(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { DetailsExecutor(get(), get(), get(), get(), get(), get()) }
    factory { StockExecutor(get(), get()) }
}
