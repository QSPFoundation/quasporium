package com.pixnpunk.details.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.moveToFile
import com.anggrayudi.storage.toStorageFile
import com.pixnpunk.database.dao.PresetDao
import com.pixnpunk.database.model.PresetEntity
import com.pixnpunk.details.data.mapper.toDomain
import com.pixnpunk.details.domain.model.PresetItem
import com.pixnpunk.details.domain.repository.PresetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Clock

internal class PresetRepositoryImpl(
    private val context: Context,
    private val presetDao: PresetDao
) : PresetRepository {

    override fun observeAllPresets(
        gameId: Long
    ): Flow<List<PresetItem>> =
        presetDao.observePresets(gameId).map { entities -> entities.toDomain() }

    override suspend fun getAllPresets(
        gameId: Long
    ): List<PresetItem> = withContext(Dispatchers.IO) {
        presetDao.getAllPresets(gameId).toDomain()
    }

    override suspend fun createPreset(
        gameId: Long,
        presetName: String,
        filePath: String?
    ): Long = withContext(Dispatchers.IO) {
        presetDao.insert(
            PresetEntity(
                gameId = gameId,
                presetName = presetName,
                lastPlayedFile = filePath,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override suspend fun updateTimestamp(
        presetId: Long
    ) = withContext(Dispatchers.IO) {
        presetDao.updateTimestamp(
            presetId = presetId,
            newTimestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun updateLastPlayedFile(
        presetId: Long,
        fileName: String
    ) = withContext(Dispatchers.IO) {
        presetDao.updateLastPlayedFile(
            presetId = presetId,
            newLastPlayedFile = fileName
        )
    }

    override suspend fun updateVisuals(
        presetId: Long,
        name: String,
        accentColor: Long
    ) = withContext(Dispatchers.IO) {
        presetDao.updateVisuals(presetId, name, accentColor)
    }

    override suspend fun saveBackgroundImage(
        presetId: Long,
        sourceUriBox: String
    ) = withContext(Dispatchers.IO) {
        val uri = sourceUriBox.toUri()
        val file = uri.toStorageFile(context) ?: return@withContext

        val backgroundsDir = File(
            context.filesDir,
            "profile_backgrounds"
        ).apply { if (!exists()) mkdirs() }

        removeOldBackgroundFile(presetId)

        val targetFile =
            File(
                backgroundsDir,
                "bg_${presetId}_${
                    Clock.System.now().toEpochMilliseconds()
                }.${MimeType.getExtensionFromMimeType(file.mimeType)}"
            ).toStorageFile(context)

        file.moveToFile(targetFile)

        val savedUri = targetFile.absolutePath ?: return@withContext
        presetDao.updateBackground(presetId, savedUri)
    }

    override suspend fun removeBackgroundImage(
        presetId: Long
    ) = withContext(Dispatchers.IO) {
        removeOldBackgroundFile(presetId)
        presetDao.updateBackground(presetId, "")
    }

    private suspend fun removeOldBackgroundFile(presetId: Long) {
        val oldPath = presetDao.getBackgroundPath(presetId)
        if (!oldPath.isNullOrBlank()) {
            val file = File(oldPath)
            if (file.exists()) file.delete()
        }
    }

    override suspend fun deletePreset(
        presetId: Long
    ) = withContext(Dispatchers.IO) {
        presetDao.delete(PresetEntity(id = presetId))
    }
}