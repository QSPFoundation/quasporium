package com.pixnpunk.game.data

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.pixnpunk.game.domain.model.AudioPlayerException
import com.pixnpunk.game.domain.repository.AudioPlayer
import com.pixnpunk.logger.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class GameAudioComponent(
    private val context: Context,
    private val logger: AppLogger
) : InstanceKeeper.Instance, AudioPlayer {

    private val playerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mediaPlayers = ConcurrentHashMap<String, MediaPlayer>()

    @Volatile private var isPaused = false
    @Volatile override var isSoundEnabled = true

    override fun onDestroy() {
        closeAllFilesSync()
        playerScope.cancel()
    }

    override fun isPlayingFile(path: String): Boolean {
        if (path.isBlank()) return false
        return mediaPlayers[path]?.isPlaying == true
    }

    private fun releasePlayer(player: MediaPlayer) {
        runCatching {
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }.onFailure {
            logger.recordNonFatalException(
                AudioPlayerException.ReleaseError(it)
            )
        }
    }

    override fun resumeAll() {
        if (isSoundEnabled && isPaused) {
            isPaused = false
            playerScope.launch {
                runCatching {
                    mediaPlayers.values.filterNot { it.isPlaying }.forEach { it.start() }
                }.onFailure {
                    logger.recordNonFatalException(
                        AudioPlayerException.PlaybackError(cause = it)
                    )
                    closeAllFilesSync()
                }
            }
        }
    }

    override fun pauseAll() {
        if (!isPaused) {
            isPaused = true
            playerScope.launch {
                runCatching {
                    mediaPlayers.values.filter { it.isPlaying }.forEach { it.pause() }
                }.onFailure {
                    logger.recordNonFatalException(
                        AudioPlayerException.PlaybackError(cause = it)
                    )
                    closeAllFilesSync()
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
                val player = mediaPlayers.computeIfAbsent(path) { keyUri ->
                    createNewSound(keyUri)
                }

                val mediaPlayerVolume = volume / 100f
                player.setVolume(mediaPlayerVolume, mediaPlayerVolume)

                if (isSoundEnabled && !isPaused) {
                    player.start()
                }
            }.onFailure {
                logger.recordNonFatalException(
                    AudioPlayerException.PlaybackError(path, it)
                )
                closeFile(path)
            }
        }
    }

    @Throws(IOException::class)
    private fun createNewSound(
        path: String
    ) = MediaPlayer().apply {
        setDataSource(context, path.toUri())
        prepare()
        setOnCompletionListener {
            playerScope.launch {
                mediaPlayers.remove(path, it)
                runCatching { it.release() }.onFailure { error ->
                    logger.recordNonFatalException(
                        AudioPlayerException.InitializationError(
                            path, error
                        )
                    )
                }
            }
        }
    }

    override fun closeFile(path: String) {
        playerScope.launch { mediaPlayers.remove(path)?.let { releasePlayer(it) } }
    }

    override fun closeAllFiles() {
        playerScope.launch { closeAllFilesSync() }
    }

    private fun closeAllFilesSync() =
        mediaPlayers.values.removeIf { releasePlayer(it); true }
}