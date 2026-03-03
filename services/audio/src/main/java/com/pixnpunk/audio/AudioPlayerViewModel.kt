package com.pixnpunk.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class AudioPlayerViewModel : ViewModel(), DefaultLifecycleObserver {
    private val audioDispatcher: CoroutineDispatcher = Dispatchers.IO
    private val mediaPlayers = ConcurrentHashMap<Uri, MediaPlayer>()
    private var isPaused = false
    var isSoundEnabled = true

    fun isPlayingFile(fileUri: Uri?): Boolean {
        return fileUri != null && fileUri != Uri.EMPTY && mediaPlayers.containsKey(fileUri)
    }

    private fun runOnAudioContext(block: suspend () -> Unit): Job =
        viewModelScope.launch(audioDispatcher) { block() }

    override fun onResume(owner: LifecycleOwner) {
        if (!isSoundEnabled || !isPaused) return
        runOnAudioContext {
            isPaused = false
            runCatching {
                mediaPlayers.values.filterNot { it.isPlaying }.forEach { it.start() }
            }.onFailure {
                FirebaseCrashlytics.getInstance().recordException(it)
                mediaPlayers.clear()
            }
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        if (isPaused) return
        runOnAudioContext {
            isPaused = true
            runCatching {
                mediaPlayers.values.filter { it.isPlaying }.forEach { it.pause() }
            }.onFailure {
                FirebaseCrashlytics.getInstance().recordException(it)
                mediaPlayers.clear()
            }
        }
    }

    fun playFile(
        context: Context,
        soundFileUri: Uri,
        volume: Int
    ) {
        runOnAudioContext {
            val mediaPlayerVolume = volume / 100f

            runCatching {
                val player = mediaPlayers.getOrPut(soundFileUri) {
                    createNewSound(context, soundFileUri)
                }

                player.setVolume(mediaPlayerVolume, mediaPlayerVolume)

                if (isSoundEnabled && !isPaused) {
                    player.start()
                }
            }.onFailure {
                FirebaseCrashlytics.getInstance().recordException(it)
                mediaPlayers.remove(soundFileUri)
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
        setOnCompletionListener { player ->
            runOnAudioContext {
                mediaPlayers.remove(filePath)
                player.release()
            }
        }
    }

    fun closeFile(filePath: Uri) {
        runOnAudioContext {
            mediaPlayers.remove(filePath)?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        }
    }

    fun closeAllFiles() {
        runOnAudioContext {
            mediaPlayers.values.forEach { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
            mediaPlayers.clear()
        }
    }

    override fun onCleared() {
        closeAllFiles()
        super.onCleared()
    }
}