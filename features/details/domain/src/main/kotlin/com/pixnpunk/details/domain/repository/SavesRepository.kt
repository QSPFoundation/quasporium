package com.pixnpunk.details.domain.repository

import com.pixnpunk.details.domain.model.SaveItem
import kotlinx.coroutines.flow.Flow

interface SavesRepository {
    fun observeAllSaves(gameId: Long): Flow<List<SaveItem>>
    suspend fun getSavesCount(gameId: Long): Int
    suspend fun getAllSaves(gameId: Long): List<SaveItem>
    suspend fun insertSave(gameId: Long, userTitle: String, filePath: String)
    suspend fun deleteSave(saveId: Long)
}
