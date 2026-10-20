package com.pixnpunk.settings.domain.usecase

import com.pixnpunk.dto.InputState
import com.pixnpunk.settings.domain.repository.SettingsRepository

class UpdateImageSizeUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(state: InputState, value: Int) =
        repository.updateImageSize(state, value)
}
