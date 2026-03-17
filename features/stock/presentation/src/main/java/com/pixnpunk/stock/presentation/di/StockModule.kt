package com.pixnpunk.stock.presentation.di

import com.pixnpunk.stock.data.di.dataModule
import com.pixnpunk.stock.domain.di.domainModule

val featureStockModules = listOf(
    dataModule,
    domainModule
)