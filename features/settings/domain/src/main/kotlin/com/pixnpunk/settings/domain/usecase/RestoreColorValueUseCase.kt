package com.pixnpunk.settings.domain.usecase

import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.settings.domain.repository.SettingsRepository

class RestoreColorValueUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(state: ColorPickerState) = repository.restoreColorValue(state)
}
