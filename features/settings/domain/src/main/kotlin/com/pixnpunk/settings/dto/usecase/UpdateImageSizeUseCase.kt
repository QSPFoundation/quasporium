package com.pixnpunk.settings.dto.usecase

import com.pixnpunk.dto.InputState
import com.pixnpunk.settings.dto.repository.SettingsRepository

class UpdateImageSizeUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(state: InputState, value: Int) =
        repository.updateImageSize(state, value)
}
