package com.pixnpunk.details.domain.repository

import com.pixnpunk.dto.LibSaveItem

interface SavesRepository {
    suspend fun getSavesCount(profileId: Long): Int
    suspend fun getAllSaves(profileId: Long): List<LibSaveItem>
}
