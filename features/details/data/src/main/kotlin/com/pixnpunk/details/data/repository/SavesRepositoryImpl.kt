package com.pixnpunk.details.data.repository

import com.pixnpunk.database.dao.SaveDao
import com.pixnpunk.database.model.SaveEntity
import com.pixnpunk.details.domain.model.SaveItem
import com.pixnpunk.details.domain.repository.SavesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class SavesRepositoryImpl(
    private val saveDao: SaveDao
) : SavesRepository {

    override fun observeAllSaves(
        gameId: Long
    ): Flow<List<SaveItem>> =
        saveDao
            .observeSaves(gameId)
            .map { entities ->
                entities.map { entity ->
                    SaveItem(
                        entity.id,
                        entity.gameId,
                        entity.userTitle,
                        entity.filePath
                    )
                }
            }

    override suspend fun getSavesCount(
        gameId: Long
    ): Int = withContext(Dispatchers.IO) {
        saveDao.getSavesCount(gameId)
    }

    override suspend fun getAllSaves(
        gameId: Long
    ): List<SaveItem> = withContext(Dispatchers.IO) {
        saveDao
            .getAllSaves(gameId)
            .map { entity ->
                SaveItem(
                    entity.id,
                    entity.gameId,
                    entity.userTitle,
                    entity.filePath
                )
            }
    }

    override suspend fun insertSave(
        gameId: Long,
        userTitle: String,
        filePath: String
    ) = withContext(Dispatchers.IO) {
        saveDao.insert(
            SaveEntity(
                gameId = gameId,
                userTitle = userTitle,
                filePath = filePath,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override suspend fun deleteSave(
        saveId: Long
    ) = withContext(Dispatchers.IO) { saveDao.delete(SaveEntity(id = saveId)) }
}
