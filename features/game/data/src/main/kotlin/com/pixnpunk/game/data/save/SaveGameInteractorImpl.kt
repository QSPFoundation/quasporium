package com.pixnpunk.game.data.save

import android.content.Context
import com.pixnpunk.database.dao.SaveDao
import com.pixnpunk.database.model.SaveEntity
import com.pixnpunk.dto.LibSaveItem
import com.pixnpunk.game.data.vfs.FileSystemProvider
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.game.domain.repository.EngineCommand
import com.pixnpunk.game.domain.repository.GameEngineInteractor
import com.pixnpunk.game.domain.repository.SaveGameInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.time.Clock
import kotlin.uuid.Uuid

internal class SaveGameInteractorImpl(
    private val context: Context,
    private val vfsProvider: FileSystemProvider,
    private val saveDao: SaveDao,
    private val engineInteractor: GameEngineInteractor
) : SaveGameInteractor {

    private companion object {
        const val AUTOSAVE_DIR = "autosaves"
    }

    override suspend fun getSavesCount(gameId: Long): Int =
        withContext(Dispatchers.IO) { saveDao.getSavesCount(gameId) }

    override suspend fun getAllSaves(gameId: Long): List<LibSaveItem> =
        withContext(Dispatchers.IO) {
            saveDao.getAllSaves(gameId).map {
                LibSaveItem(it.userTitle, it.timestamp, it.filePath)
            }
        }

    override suspend fun createAndSaveSlot(gameId: Long, saveName: String, gameDir: VfsPath) =
        withContext(Dispatchers.Default) {
            val gameRoot = vfsProvider.resolve(gameDir) ?: return@withContext

            val savesDir = gameRoot.makeDir("QuasporiumSaves") ?: return@withContext

            val existSave = saveDao.searchByTitle(gameId, saveName)

            val newSaveFile = if (existSave == null) {
                val fileName = "Save_${Uuid.random().toHexString()}_.sav"
                val file = savesDir.makeFile(fileName) ?: return@withContext

                saveDao.insert(
                    SaveEntity(
                        gameId = gameId,
                        userTitle = saveName,
                        filePath = file.uriPath.value,
                        timestamp = Clock.System.now().toEpochMilliseconds()
                    )
                )

                file
            } else {
                val existFile = vfsProvider.resolve(VfsPath(existSave.filePath))
                existFile?.delete()

                val fileName = existFile?.name ?: "Save_${Uuid.random().toHexString()}_.sav"
                val file = savesDir.makeFile(fileName) ?: return@withContext

                saveDao.update(
                    existSave.copy(
                        userTitle = saveName,
                        filePath = file.uriPath.value,
                        timestamp = Clock.System.now().toEpochMilliseconds()
                    )
                )

                file
            }

            engineInteractor.submitCommand(EngineCommand.SaveGame(newSaveFile.uriPath))
        }

    override suspend fun autosave(gameFile: VfsPath) =
        withContext(Dispatchers.IO) {
            val file = autosaveFile(gameFile)
            file.parentFile?.mkdirs()
            if (!file.exists() && !file.createNewFile()) return@withContext

            engineInteractor.submitCommand(EngineCommand.SaveGame(VfsPath(file.absolutePath)))
        }

    override suspend fun findAutosave(gameFile: VfsPath): VfsPath? =
        withContext(Dispatchers.IO) {
            autosaveFile(gameFile).takeIf { it.length() > 0 }?.let { VfsPath(it.absolutePath) }
        }

    override suspend fun deleteAutosave(gameFile: VfsPath) {
        withContext(Dispatchers.IO) { autosaveFile(gameFile).delete() }
    }

    private fun autosaveFile(gameFile: VfsPath): File {
        val key = UUID.nameUUIDFromBytes(gameFile.value.toByteArray()).toString()
        return File(File(context.filesDir, AUTOSAVE_DIR), "$key.sav")
    }
}
