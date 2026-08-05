package com.pixnpunk.game.data.save

import com.pixnpunk.database.dao.SaveSlotDao
import com.pixnpunk.database.model.SaveSlotEntity
import com.pixnpunk.dto.LibSaveItem
import com.pixnpunk.game.data.vfs.FileSystemProvider
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.game.domain.repository.EngineCommand
import com.pixnpunk.game.domain.repository.GameEngineInteractor
import com.pixnpunk.game.domain.repository.SaveGameInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class SaveGameInteractorImpl(
    private val vfsProvider: FileSystemProvider,
    private val saveSlotDao: SaveSlotDao,
    private val engineInteractor: GameEngineInteractor
) : SaveGameInteractor {

    override suspend fun getSavesCount(gameId: Long): Int =
        withContext(Dispatchers.IO) { saveSlotDao.getSavesCount(gameId) }

    override suspend fun getAllSaves(gameId: Long): List<LibSaveItem> =
        withContext(Dispatchers.IO) {
            saveSlotDao.getAllSaves(gameId).map {
                LibSaveItem(it.userTitle, it.timestamp, it.filePath)
            }
        }

    override suspend fun createAndSaveSlot(gameId: Long, saveName: String, gameDir: VfsPath) =
        withContext(Dispatchers.Default) {
            val gameRoot = vfsProvider.resolve(gameDir) ?: return@withContext

            val savesDir = gameRoot.makeDir("QuasporiumSaves") ?: return@withContext

            val existSave = saveSlotDao.searchByTitle(gameId, saveName)

            val newSaveFile = if (existSave == null) {
                val fileName = "Save_${Uuid.random().toHexString()}_.sav"
                val file = savesDir.makeFile(fileName) ?: return@withContext

                saveSlotDao.insert(
                    SaveSlotEntity(
                        gameId = gameId,
                        userTitle = saveName,
                        filePath = file.path.value,
                        timestamp = Clock.System.now().toEpochMilliseconds()
                    )
                )

                file
            } else {
                val existFile = vfsProvider.resolve(VfsPath(existSave.filePath))
                existFile?.delete()

                val fileName = existFile?.name ?: "Save_${Uuid.random().toHexString()}_.sav"
                val file = savesDir.makeFile(fileName) ?: return@withContext

                saveSlotDao.update(
                    existSave.copy(
                        userTitle = saveName,
                        filePath = file.path.value,
                        timestamp = Clock.System.now().toEpochMilliseconds()
                    )
                )

                file
            }

            engineInteractor.submitCommand(EngineCommand.SaveGame(newSaveFile.path))
        }
}
