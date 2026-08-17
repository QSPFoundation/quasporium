package com.pixnpunk.details.data.repository

import android.content.Context
import androidx.core.net.toUri
import com.pixnpunk.database.dao.ProfileDao
import com.pixnpunk.database.model.ProfileEntity
import com.pixnpunk.details.domain.model.ProfileItem
import com.pixnpunk.details.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Clock

internal class ProfileRepositoryImpl(
    private val context: Context,
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun observeProfiles(gameId: Long): Flow<List<ProfileItem>> =
        profileDao.observeProfilesByGameId(gameId).map { entities ->
            entities.map { entity ->
                ProfileItem(
                    id = entity.id,
                    name = entity.profileName,
                    timestamp = entity.timestamp,
                    lastPlayedFile = entity.lastPlayedFile,
                    accentColor = entity.accentColor,
                    backImagePath = entity.backImagePath
                )
            }
        }

    override suspend fun createProfile(
        gameId: Long,
        profileName: String,
        filePath: String?
    ): Long = withContext(Dispatchers.IO) {
        profileDao.insert(
            ProfileEntity(
                gameId = gameId,
                profileName = profileName,
                lastPlayedFile = filePath,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    override suspend fun updateProfileTimestamp(
        profileId: Long
    ) = withContext(Dispatchers.IO) {
        profileDao.updateTimestamp(
            profileId = profileId,
            newTimestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun updateLastPlayedFile(
        profileId: Long,
        fileName: String
    ) = withContext(Dispatchers.IO) {
        profileDao.updateLastPlayedFile(
            profileId = profileId,
            newLastPlayedFile = fileName
        )
    }

    override suspend fun updateProfileVisuals(
        profileId: Long,
        name: String,
        accentColor: Long
    ) = withContext(Dispatchers.IO) {
        profileDao.updateVisuals(profileId, name, accentColor)
    }

    override suspend fun saveBackgroundImage(
        profileId: Long,
        sourceUriBox: String
    ): String = withContext(Dispatchers.IO) {
        val uri = sourceUriBox.toUri()
        val backgroundsDir = File(
            context.filesDir,
            "profile_backgrounds"
        ).apply { if (!exists()) mkdirs() }

        // removeOldBackgroundFile(profileId)

        val targetFile =
            File(
                backgroundsDir,
                "bg_${profileId}_${Clock.System.now().toEpochMilliseconds()}.jpg"
            )

//        context.contentResolver.openInputStream(uri)?.use { input ->
//            FileOutputStream(targetFile).use { output ->
//                val buffer = ByteArray(8192)
//                var bytesRead: Int
//                while (input.read(buffer).also { bytesRead = it } != -1) {
//                    output.write(buffer, 0, bytesRead)
//                }
//                output.flush()
//            }
//        } ?: throw IllegalStateException("Unable to open InputStream for URI: $sourceUriString")

        val savedPath = targetFile.absolutePath
        profileDao.updateBackground(profileId, savedPath)
        savedPath
    }

    override suspend fun removeBackgroundImage(
        profileId: Long
    ) = withContext(Dispatchers.IO) {
        removeOldBackgroundFile(profileId)
        profileDao.updateBackground(profileId, "")
    }

    private suspend fun removeOldBackgroundFile(profileId: Long) {
        val oldPath = profileDao.getBackgroundPath(profileId)
        if (!oldPath.isNullOrBlank()) {
            val file = File(oldPath)
            if (file.exists()) file.delete()
        }
    }

    override suspend fun deleteProfile(
        profileId: Long
    ) = withContext(Dispatchers.IO) {
        profileDao.delete(ProfileEntity(id = profileId))
    }
}