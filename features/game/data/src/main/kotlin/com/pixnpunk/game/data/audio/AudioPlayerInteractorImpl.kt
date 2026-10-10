package com.pixnpunk.game.data.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import com.pixnpunk.game.data.vfs.FileSystemProvider
import com.pixnpunk.game.data.vfs.impl.DirectVirtualFile
import com.pixnpunk.game.domain.model.AudioPlayerException
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.game.domain.repository.AudioPlayerInteractor
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.logger.coroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

internal class AudioPlayerInteractorImpl(
    private val context: Context,
    private val logger: AppLogger,
    private val vfsProvider: FileSystemProvider
) : AudioPlayerInteractor {

    private val playerDispatcher = Dispatchers.IO.limitedParallelism(1)
    private val playerScope = CoroutineScope(
        playerDispatcher + SupervisorJob() + logger.coroutineExceptionHandler("AudioPlayer")
    )

    private val mediaPlayers = ConcurrentHashMap<String, MediaPlayer>()

    @Volatile private var isPaused = false
    @Volatile override var isSoundEnabled = true

    internal fun close() {
        playerScope.cancel()
        closeAllFilesSync()
    }

    override fun isPlayingFile(path: String): Boolean =
        path.isNotBlank() && mediaPlayers[path]?.safeIsPlaying == true

    override fun resumeAll() {
        if (isSoundEnabled && isPaused) {
            isPaused = false

            playerScope.launch {
                mediaPlayers.values.forEach { player ->
                    player.safeAction { if (!safeIsPlaying) start() }
                }
            }
        }
    }

    override fun pauseAll() {
        if (!isPaused) {
            isPaused = true

            playerScope.launch {
                mediaPlayers.values.forEach { player ->
                    player.safeAction { if (safeIsPlaying) pause() }
                }
            }
        }
    }

    override fun playFile(
        path: String,
        volume: Int
    ) {
        if (path.isBlank()) return

        playerScope.launch {
            runCatching {
                val player = mediaPlayers.computeIfAbsent(
                    path,
                    ::createNewSound
                )

                val mediaPlayerVolume = volume / 100f
                player.setVolume(mediaPlayerVolume, mediaPlayerVolume)

                if (isSoundEnabled && !isPaused) {
                    player.start()
                }
            }.onFailure {
                logger.recordNonFatalException(
                    AudioPlayerException.PlaybackError(path, it)
                )
                closeFileSync(path)
            }
        }
    }

    private fun createNewSound(path: String): MediaPlayer =
        MediaPlayer().apply {
            runCatching {
                vfsProvider.resolve(
                    VfsPath(path)
                )?.let {
                    if (it is DirectVirtualFile) {
                        setDataSource(it.absPath.value)
                        prepare()
                    } else {
                        setDataSource(context, path.toUri())
                        prepare()
                    }
                }
            }.onFailure { error ->
                runCatching { release() }
                throw error
            }

            setOnErrorListener { _, what, extra ->
                logger.recordNonFatalException(
                    RuntimeException(
                        "MediaPlayer async error: what=$what extra=$extra path=$path"
                    )
                )
                closeFile(path)
                true
            }

            setOnCompletionListener { p ->
                playerScope.launch {
                    mediaPlayers.remove(path, p)
                    p.safeRelease()
                }
            }
        }

    override fun closeFile(path: String) {
        playerScope.launch { closeFileSync(path) }
    }

    override fun closeAllFiles() {
        playerScope.launch { closeAllFilesSync() }
    }

    private fun closeFileSync(path: String) {
        mediaPlayers.remove(path)?.safeRelease()
    }

    private fun closeAllFilesSync() {
        mediaPlayers.values.forEach { it.safeRelease() }
        mediaPlayers.clear()
    }

    private val MediaPlayer.safeIsPlaying: Boolean
        get() = runCatching { isPlaying }.getOrDefault(false)

    private inline fun MediaPlayer.safeAction(
        action: MediaPlayer.() -> Unit
    ) = runCatching { action() }.onFailure { error ->
        logger.recordNonFatalException(
            AudioPlayerException.PlaybackError(cause = error)
        )
    }

    private fun MediaPlayer.safeRelease() {
        runCatching {
            setOnErrorListener(null)
            setOnCompletionListener(null)
            if (safeIsPlaying) stop()
        }

        runCatching { release() }.onFailure { error ->
            logger.recordNonFatalException(
                AudioPlayerException.ReleaseError(error)
            )
        }
    }
}