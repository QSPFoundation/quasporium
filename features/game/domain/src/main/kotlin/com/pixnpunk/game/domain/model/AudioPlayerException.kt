package com.pixnpunk.game.domain.model

sealed class AudioPlayerException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {

    class InitializationError(cause: Throwable) :
        AudioPlayerException("Failed to initialize the player", cause)

    class PlaybackError(cause: Throwable) :
        AudioPlayerException("An error occurred while playing the file", cause)

    class ReleaseError(cause: Throwable) :
        AudioPlayerException("Error releasing player resources", cause)
}
