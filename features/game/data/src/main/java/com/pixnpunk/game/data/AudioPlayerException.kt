package com.pixnpunk.game.data

import android.net.Uri

sealed class AudioPlayerException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class InitializationError(val uri: Uri, cause: Throwable) :
        AudioPlayerException("Failed to initialize the player for $uri", cause)

    class PlaybackError(val uri: Uri, cause: Throwable) :
        AudioPlayerException("An error occurred while playing the file $uri", cause)

    class ReleaseError(cause: Throwable) :
        AudioPlayerException("Error releasing player resources", cause)
}