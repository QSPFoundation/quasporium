package com.pixnpunk.details.domain.repository

import com.pixnpunk.details.domain.model.SaveItem
import kotlinx.coroutines.flow.Flow

interface SavesRepository {
    fun observeAllSaves(gameId: Long): Flow<List<SaveItem>>
    suspend fun getSavesCount(profileId: Long): Int
    suspend fun getAllSaves(profileId: Long): List<SaveItem>
    suspend fun insertSave(profileId: Long, fileName: String, filePath: String)
    suspend fun deleteSave(saveId: Long)
}
