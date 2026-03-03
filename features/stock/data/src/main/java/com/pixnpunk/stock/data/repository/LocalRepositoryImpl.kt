package com.pixnpunk.stock.data.repository

import androidx.core.net.toUri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.deleteRecursively
import com.anggrayudi.storage.file.isRawFile
import com.pixnpunk.database.dao.GameDao
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.GameListType
import com.pixnpunk.stock.data.mapper.toDomain
import com.pixnpunk.stock.data.mapper.toEntity
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.utils.DirUtil.receiveGameFilesFromDir
import com.pixnpunk.utils.DirUtil.tryReceiveDirSize
import com.pixnpunk.utils.FileUtil.createSpecialFiles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class LocalRepositoryImpl : LocalRepository, KoinComponent {

    private val gameDao: GameDao by inject()

    override fun observeEntryUrls(): Flow<List<String>> =
        gameDao.getByUrl().distinctUntilChanged()

    override fun observeEntryById(entryId: Long): Flow<GameEntry> =
        gameDao.subscribeById(entryId).distinctUntilChanged().map { it.toDomain() }

    override suspend fun deleteGameEntries(idsDelete: Set<Long>): Int {
        val entriesDelete = gameDao.getByIds(idsDelete).apply {
            forEach {
                val gameDir = it.gameDirPath.toUri().toDocumentFile(get())
                if (gameDir?.isRawFile == true) {
                    gameDir.deleteRecursively(get())
                }
            }
        }
        return gameDao.deleteAll(entriesDelete)
    }

    override suspend fun getGameEntryById(entryId: Long): GameEntry =
        gameDao.getById(entryId)?.toDomain() ?: GameEntry()

    override suspend fun getAllPagedGameEntries(): Flow<PagingData<GameEntry>> =
        Pager(
            config = PagingConfig(
                pageSize = 15,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { gameDao.getAllPaged() }
        ).flow.map { data -> data.map { entry -> entry.toDomain() } }

    override suspend fun searchGameEntriesByQuery(query: String): Flow<PagingData<GameEntry>> =
        Pager(
            config = PagingConfig(
                pageSize = 15,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { gameDao.search(query) }
        ).flow.map { data -> data.map { entry -> entry.toDomain() } }

    override suspend fun updateEntry(entry: GameEntry) {
        val nullEntry = gameDao.getById(entry.id)
        if (nullEntry != null) {
            gameDao.update(entry.toEntity())
        }
    }

    override suspend fun updateOrInsertEntry(entry: GameEntry) {
        val nullEntry = gameDao.getById(entry.id)
        if (nullEntry != null) {
            gameDao.update(entry.toEntity())
        } else {
            insertDefaultEntry(entry)
        }
    }

    override suspend fun insertDefaultEntry(newEntry: GameEntry) {
        gameDao.insert(newEntry.toEntity())
    }

    override suspend fun fillAndInsertEntry(entry: GameEntry) {
        val gameDirPath = entry.gameDirPath
        createSpecialFiles(gameDirPath)
        insertDefaultEntry(
            entry.copy(
                listId = GameListType.Playable,
                gameFilesPath = receiveGameFilesFromDir(gameDirPath),
                fileSize =
                    if (entry.fileSize != 0L) entry.fileSize
                    else tryReceiveDirSize(gameDirPath)
            )
        )
    }
}