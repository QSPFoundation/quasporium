package com.pixnpunk.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.anggrayudi.storage.file.DocumentFileCompat.fromUri
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
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
        val gameDir = fromUri(
            context = get(),
            uri = entry.gameDirPath.toUri(),
        )
        val gameFile = fromUri(
            context = get(),
            uri = entry.gameFilesPath[fileSelIndex].toUri(),
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