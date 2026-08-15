package com.pixnpunk.details.data.repository

import com.pixnpunk.database.dao.SaveSlotDao
import com.pixnpunk.details.domain.repository.SavesRepository
import com.pixnpunk.dto.LibSaveItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SavesRepositoryImpl(
    private val saveSlotDao: SaveSlotDao
) : SavesRepository {

    override suspend fun getSavesCount(profileId: Long): Int =
        withContext(Dispatchers.IO) { saveSlotDao.getSavesCount(profileId) }

    override suspend fun getAllSaves(profileId: Long): List<LibSaveItem> =
        withContext(Dispatchers.IO) {
            saveSlotDao.getAllSaves(profileId).map {
                LibSaveItem(it.userTitle, it.timestamp, it.filePath)
            }
        }
}
