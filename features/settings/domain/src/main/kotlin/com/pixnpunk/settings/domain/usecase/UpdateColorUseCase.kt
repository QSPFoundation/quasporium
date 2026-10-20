package com.pixnpunk.settings.domain.usecase

import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.settings.domain.repository.SettingsRepository

class UpdateColorUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(state: ColorPickerState, colorArgb: Int) =
        repository.updateColorValue(state, colorArgb)
}
