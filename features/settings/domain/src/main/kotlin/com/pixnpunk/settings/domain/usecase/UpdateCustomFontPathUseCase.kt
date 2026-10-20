package com.pixnpunk.settings.domain.usecase

import com.pixnpunk.settings.domain.repository.SettingsRepository

class UpdateCustomFontPathUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(filePath: String) =
        repository.updateCustomFontPath(filePath)
}
