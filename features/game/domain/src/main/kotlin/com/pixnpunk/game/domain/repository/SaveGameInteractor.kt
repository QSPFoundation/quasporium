package com.pixnpunk.game.domain.repository

import com.pixnpunk.dto.LibSaveItem
import com.pixnpunk.game.domain.model.GameId
import com.pixnpunk.game.domain.model.VfsPath

interface SaveGameInteractor {
    suspend fun getSavesCount(profileId: Long): Int
    suspend fun getAllSaves(profileId: Long): List<LibSaveItem>
    suspend fun createAndSaveSlot(profileId: Long, saveName: String, gameDir: VfsPath)
}
