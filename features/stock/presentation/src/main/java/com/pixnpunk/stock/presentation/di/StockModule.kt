package com.pixnpunk.stock.presentation.di

import com.pixnpunk.stock.data.di.dataModule
import com.pixnpunk.stock.domain.di.domainModule
import com.pixnpunk.stock.presentation.SourceViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sourceModule = module {
    singleOf(::SourceViewModel)
}

val featureStockModules = listOf(
    dataModule,
    domainModule,
    sourceModule
)