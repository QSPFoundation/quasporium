package com.pixnpunk.details.data.repository

import com.pixnpunk.database.dao.ProfileDao
import com.pixnpunk.database.model.ProfileEntity
import com.pixnpunk.details.domain.model.ProfileItem
import com.pixnpunk.details.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class ProfileRepositoryImpl(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override fun observeProfiles(gameId: Long): Flow<List<ProfileItem>> =
        profileDao.observeProfilesByGameId(gameId).map { entities ->
            entities.map { entity ->
                ProfileItem(
                    id = entity.id,
                    name = entity.profileName,
                    timestamp = entity.timestamp,
                    lastPlayedFile = entity.lastPlayedFile
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

    override suspend fun deleteProfile(
        profileId: Long
    ) = withContext(Dispatchers.IO) {
        profileDao.delete(ProfileEntity(id = profileId))
    }
}