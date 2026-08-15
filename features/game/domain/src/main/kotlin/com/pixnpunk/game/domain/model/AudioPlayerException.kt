package com.pixnpunk.game.domain.model

sealed class AudioPlayerException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {

    class InitializationError(uriBox: String = "", cause: Throwable) :
        AudioPlayerException("Failed to initialize the player for $uriBox", cause)

    class PlaybackError(uriBox: String = "", cause: Throwable) :
        AudioPlayerException("An error occurred while playing the file $uriBox", cause)

    class ReleaseError(cause: Throwable) :
        AudioPlayerException("Error releasing player resources", cause)
}
