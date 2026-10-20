package com.pixnpunk.quasporium.di

import com.pixnpunk.settings.domain.usecase.GetSettingsUseCase
import com.pixnpunk.settings.domain.usecase.RestoreColorValueUseCase
import com.pixnpunk.settings.domain.usecase.UpdateColorUseCase
import com.pixnpunk.settings.domain.usecase.UpdateCustomFontPathUseCase
import com.pixnpunk.settings.domain.usecase.UpdateImageSizeUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val settingsDomainModule = module {
    factoryOf(::GetSettingsUseCase)
    factoryOf(::UpdateColorUseCase)
    factoryOf(::RestoreColorValueUseCase)
    factoryOf(::UpdateImageSizeUseCase)
    factoryOf(::UpdateCustomFontPathUseCase)
}
