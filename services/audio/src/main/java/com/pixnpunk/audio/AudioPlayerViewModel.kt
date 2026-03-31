package com.pixnpunk.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixnpunk.logger.AppLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class AudioPlayerViewModel(
    private val logger: AppLogger,
) : ViewModel(), DefaultLifecycleObserver {

    private val audioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val mediaPlayers = ConcurrentHashMap<Uri, MediaPlayer>()
    @Volatile private var isPaused = false
    @Volatile var isSoundEnabled = true

    fun isPlayingFile(fileUri: Uri?): Boolean {
        return fileUri != null && fileUri != Uri.EMPTY && mediaPlayers.containsKey(fileUri)
    }

    private fun runOnAudioContext(block: suspend () -> Unit): Job =
        viewModelScope.launch(audioDispatcher) { block() }

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

    override fun onResume(owner: LifecycleOwner) {
        if (isSoundEnabled && isPaused) {
            isPaused = false
            runOnAudioContext {
                runCatching {
                    mediaPlayers.values.filterNot { it.isPlaying }.forEach { it.start() }
                }.onFailure {
                    logger.recordNonFatalException(
                        AudioPlayerException.PlaybackError(Uri.EMPTY, it)
                    )
                    closeAllFilesSync()
                }
            }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        if (!isPaused) {
            isPaused = true
            runOnAudioContext {
                runCatching {
                    mediaPlayers.values.filter { it.isPlaying }.forEach { it.pause() }
                }.onFailure {
                    logger.recordNonFatalException(
                        AudioPlayerException.PlaybackError(Uri.EMPTY, it)
                    )
                    closeAllFilesSync()
                }
            }
        }
    }

    fun playFile(
        context: Context,
        soundFileUri: Uri,
        volume: Int
    ) {
        runOnAudioContext {
            runCatching {
                val player = mediaPlayers.computeIfAbsent(soundFileUri) { keyUri ->
                    createNewSound(context, keyUri)
                }

                val mediaPlayerVolume = volume / 100f
                player.setVolume(mediaPlayerVolume, mediaPlayerVolume)

                if (isSoundEnabled && !isPaused) {
                    player.start()
                }
            }.onFailure {
                logger.recordNonFatalException(
                    AudioPlayerException.PlaybackError(soundFileUri, it)
                )
                closeFile(soundFileUri)
            }
        }
    }

    @Throws(IOException::class)
    private fun createNewSound(
        context: Context,
        filePath: Uri
    ) = MediaPlayer().apply {
        setDataSource(context, filePath)
        prepare()
        setOnCompletionListener {
            runOnAudioContext {
                mediaPlayers.remove(filePath, it)
                runCatching { it.release() }.onFailure { error ->
                    logger.recordNonFatalException(
                        AudioPlayerException.InitializationError(filePath, error)
                    )
                }
            }
        }
    }

    fun closeFile(filePath: Uri) {
        runOnAudioContext { mediaPlayers.remove(filePath)?.let { releasePlayer(it) } }
    }

    fun closeAllFiles() {
        runOnAudioContext { closeAllFilesSync() }
    }

    private fun closeAllFilesSync() =
        mediaPlayers.values.removeIf { releasePlayer(it); true }

    override fun onCleared() {
        closeAllFiles()
        super.onCleared()
    }
}