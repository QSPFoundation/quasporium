package com.pixnpunk.settings.dto.di

import com.pixnpunk.settings.dto.usecase.GetSettingsUseCase
import com.pixnpunk.settings.dto.usecase.RestoreColorValueUseCase
import com.pixnpunk.settings.dto.usecase.UpdateColorUseCase
import com.pixnpunk.settings.dto.usecase.UpdateCustomFontPathUseCase
import com.pixnpunk.settings.dto.usecase.UpdateImageSizeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val settingsDomainModule = module {
    factoryOf(::GetSettingsUseCase)
    factoryOf(::UpdateColorUseCase)
    factoryOf(::RestoreColorValueUseCase)
    factoryOf(::UpdateImageSizeUseCase)
    factoryOf(::UpdateCustomFontPathUseCase)
}
