package com.pixnpunk.storage

interface GameFileStorage {
    suspend fun receiveGameFiles(rootDirPath: String): List<String>

    suspend fun receiveGameSaves(rootDirPath: String): List<String>?

    fun receiveFullName(path: String): String

    fun receiveBaseName(path: String): String
}

internal class SafGameFileStorage : GameFileStorage {
    override suspend fun receiveGameFiles(rootDirPath: String): List<String> =
        DirUtil.receiveGameFilesFromDir(rootDirPath)

    override suspend fun receiveGameSaves(rootDirPath: String): List<String>? =
        DirUtil.receiveGameSavesFromDir(rootDirPath)

    override fun receiveFullName(path: String): String =
        with(FileUtil) { path.receiveFullName() }

    override fun receiveBaseName(path: String): String =
        with(FileUtil) { path.receiveBaseName() }
}
