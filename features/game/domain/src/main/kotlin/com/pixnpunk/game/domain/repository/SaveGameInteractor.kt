package com.pixnpunk.game.domain.repository

import com.pixnpunk.dto.LibSaveItem
import com.pixnpunk.game.domain.model.VfsPath

interface SaveGameInteractor {
    suspend fun getSavesCount(gameId: Long): Int
    suspend fun getAllSaves(gameId: Long): List<LibSaveItem>
    suspend fun createAndSaveSlot(gameId: Long, saveName: String, gameDir: VfsPath)
    suspend fun autosave(gameFile: VfsPath)
    suspend fun findAutosave(gameFile: VfsPath): VfsPath?
    suspend fun deleteAutosave(gameFile: VfsPath)
}
