package com.pixnpunk.settings.dto.usecase

import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.settings.dto.repository.SettingsRepository

class UpdateColorUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(state: ColorPickerState, colorArgb: Int) =
        repository.updateColorValue(state, colorArgb)
}
