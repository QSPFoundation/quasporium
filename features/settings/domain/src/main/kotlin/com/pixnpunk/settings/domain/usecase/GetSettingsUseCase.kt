package com.pixnpunk.settings.domain.usecase

import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class GetSettingsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): StateFlow<SettingsEntity> = repository.settingsFlow
}
