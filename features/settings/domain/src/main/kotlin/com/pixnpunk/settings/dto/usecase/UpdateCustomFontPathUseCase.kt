package com.pixnpunk.settings.dto.usecase

import com.pixnpunk.settings.dto.repository.SettingsRepository

class UpdateCustomFontPathUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(filePath: String) =
        repository.updateCustomFontPath(filePath)
}
