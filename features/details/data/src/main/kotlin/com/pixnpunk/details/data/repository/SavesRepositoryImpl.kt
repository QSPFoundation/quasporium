package com.pixnpunk.details.data.repository

import com.pixnpunk.database.dao.SaveSlotDao
import com.pixnpunk.database.model.SaveSlotEntity
import com.pixnpunk.details.domain.model.SaveItem
import com.pixnpunk.details.domain.repository.SavesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class SavesRepositoryImpl(
    private val saveSlotDao: SaveSlotDao
) : SavesRepository {

    override fun observeAllSaves(
        gameId: Long
    ): Flow<List<SaveItem>> =
        saveSlotDao
            .observeAllSavesForGame(gameId)
            .map { entities ->
                entities.map { entity ->
                    SaveItem(
                        entity.id,
                        entity.profileId,
                        entity.userTitle,
                        entity.filePath
                    )
                }
            }

    override suspend fun getSavesCount(
        profileId: Long
    ): Int = withContext(Dispatchers.IO) {
        saveSlotDao.getSavesCount(profileId)
    }

    override suspend fun getAllSaves(
        profileId: Long
    ): List<SaveItem> = withContext(Dispatchers.IO) {
        saveSlotDao
            .getAllSaves(profileId)
            .map { entity ->
                SaveItem(
                    entity.id,
                    entity.profileId,
                    entity.userTitle,
                    entity.filePath
                )
            }
    }

    override suspend fun insertSave(
        profileId: Long,
        fileName: String,
        filePath: String
    ) = withContext(Dispatchers.IO) {
        saveSlotDao.insert(
            SaveSlotEntity(
                profileId = profileId,
                userTitle = fileName,
                filePath = filePath,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override suspend fun deleteSave(
        saveId: Long
    ) = withContext(Dispatchers.IO) { saveSlotDao.delete(SaveSlotEntity(id = saveId)) }
}
