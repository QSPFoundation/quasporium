package com.pixnpunk.settings.dto.usecase

import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.settings.dto.repository.SettingsRepository

class RestoreColorValueUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(state: ColorPickerState) = repository.restoreColorValue(state)
}
