package com.pixnpunk.settings.dto.usecase

import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.dto.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class GetSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): StateFlow<SettingsEntity> = repository.settingsFlow
}
