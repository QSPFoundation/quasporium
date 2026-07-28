package com.pixnpunk.settings.presentation.di

import com.pixnpunk.settings.data.di.settingsDataModule
import com.pixnpunk.settings.dto.di.settingsDomainModule

val featureSettingsModule = listOf(
    settingsDataModule,
    settingsDomainModule
)
