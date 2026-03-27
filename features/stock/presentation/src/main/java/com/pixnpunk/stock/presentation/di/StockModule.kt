package com.pixnpunk.stock.presentation.di

import com.pixnpunk.stock.data.di.stockDataModule
import com.pixnpunk.stock.domain.di.stockDomainModule

val featureStockModules = listOf(
    stockDataModule,
    stockDomainModule
)