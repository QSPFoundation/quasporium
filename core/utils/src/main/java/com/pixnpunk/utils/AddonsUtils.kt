package com.pixnpunk.utils

import android.content.Context
import android.content.Intent
import com.anggrayudi.storage.file.DocumentFileCompat
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.contracts.ExperimentalContracts

object AddonsUtils : KoinComponent {

    @OptIn(ExperimentalContracts::class)
    fun <T> createEngineIntent(
        clazz: Class<T>,
        entry: GameEntry,
        fileSelIndex: Int
    ): Intent {
        val gameDir = DocumentFileCompat.fromFullPath(
            context = get(),
            fullPath = entry.gameDirPath,
            requiresWriteAccess = true
        )
        val gameFile = DocumentFileCompat.fromFullPath(
            context = get(),
            fullPath = entry.gameFilesPath[fileSelIndex],
            requiresWriteAccess = true
        )

        return if (!gameDir.isWritableDir() || !gameFile.isWritableFile()) {
            Intent()
        } else {
            Intent(get<Context>(), clazz).apply {
                putExtra("gameId", entry.id)
                putExtra("gameTitle", entry.title)
                putExtra("gameDirUri", gameDir.uri.toString())
                putExtra("gameFileUri", gameFile.uri.toString())
            }
        }
    }
}