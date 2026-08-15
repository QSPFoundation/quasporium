package com.pixnpunk.details.presentation.di

import com.pixnpunk.details.data.di.detailsDataModule
import com.pixnpunk.details.domain.di.detailsDomainModule

val featureDetailsModule = listOf(
    detailsDataModule,
    detailsDomainModule
)
