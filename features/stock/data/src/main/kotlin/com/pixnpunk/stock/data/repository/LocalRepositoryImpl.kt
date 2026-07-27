package com.pixnpunk.stock.data.repository

import android.content.Context
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
import com.pixnpunk.stock.domain.model.SearchState
import com.pixnpunk.stock.domain.model.SearchTokenType
import com.pixnpunk.stock.domain.model.SortOrder
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.utils.DirUtil.receiveGameFilesFromDir
import com.pixnpunk.utils.DirUtil.tryReceiveDirSize
import com.pixnpunk.utils.FileUtil.createSpecialFiles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class LocalRepositoryImpl(
    private val context: Context,
    private val gameDao: GameDao
) : LocalRepository {

    override fun observeEntryUrls(): Flow<List<String>> =
        gameDao.getByUrl().distinctUntilChanged()

    override fun observeEntryById(entryId: Long): Flow<GameEntry?> =
        gameDao.subscribeById(entryId).distinctUntilChanged().map { it?.toDomain() }

    override suspend fun deleteGameEntries(idsDelete: Set<Long>): Int {
        val entriesDelete = gameDao.getByIds(idsDelete).apply {
            forEach {
                val gameDir = it.gameDirPath.toUri().toDocumentFile(context)
                if (gameDir?.isRawFile == true) {
                    gameDir.deleteRecursively(context)
                }
            }
        }
        return gameDao.deleteAll(entriesDelete)
    }

    override suspend fun getGameEntryById(entryId: Long): GameEntry =
        gameDao.getById(entryId)?.toDomain() ?: GameEntry()

    override fun getGameEntriesPager(
        searchState: SearchState
    ): Flow<PagingData<GameEntry>> =
        Pager(
            config = PagingConfig(
                pageSize = 15,
                initialLoadSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                val ftsQuery = buildList {
                    val cleanTitle = searchState.query
                        .filter { it.isLetterOrDigit() || it.isWhitespace() }
                    if (cleanTitle.isNotBlank()) {
                        val titleQuery = cleanTitle
                            .split(' ')
                            .filter { it.isNotBlank() }
                            .joinToString(" ") { "title:$it*" }
                        add(titleQuery)
                    }

                    searchState.activeTokens.forEach { token ->
                        val cleanTokenValue = token.value
                            .filter { it.isLetterOrDigit() || it.isWhitespace() }
                        if (cleanTokenValue.isNotEmpty()) {
                            when (token.type) {
                                SearchTokenType.AUTHOR -> "author"
                                SearchTokenType.TRANSLATOR -> "portedBy"
                                SearchTokenType.DESCRIPTION -> "descUrl"
                                else -> null
                            }?.let { column ->
                                add(
                                    cleanTokenValue
                                        .split(' ')
                                        .filter { it.isNotBlank() }
                                        .joinToString(" ") { "$column:$it*" }
                                )
                            }
                        }
                    }
                }.joinToString(separator = " ")

                when (searchState.sortOrder) {
                    SortOrder.ASCENDING -> {
                        if (ftsQuery.isBlank()) {
                            gameDao.getAllAsc()
                        } else {
                            gameDao.searchAsc(ftsQuery)
                        }
                    }

                    SortOrder.DESCENDING -> {
                        if (ftsQuery.isBlank()) {
                            gameDao.getAllDesc()
                        } else {
                            gameDao.searchDesc(ftsQuery)
                        }
                    }
                }
            }
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
