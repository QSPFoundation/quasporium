package com.pixnpunk.details.domain.repository

import com.pixnpunk.details.domain.model.ProfileItem
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeProfiles(gameId: Long): Flow<List<ProfileItem>>
    suspend fun createProfile(gameId: Long, profileName: String, filePath: String? = null): Long
    suspend fun deleteProfile(profileId: Long)
    suspend fun updateProfileTimestamp(profileId: Long)
    suspend fun updateLastPlayedFile(profileId: Long, fileName: String)
}
