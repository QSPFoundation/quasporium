package com.pixnpunk.game.data.engine.jni

import com.pixnpunk.game.data.model.RawItem
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.model.WindowType

internal interface NativeEngineListener {
    fun onStateRefreshed(
        mainDesc: String,
        varsDesc: String,
        uiConfig: LibUIConfig,
        actions: List<RawItem>,
        objects: List<RawItem>
    )

    fun onError(details: String)

    fun onSetTimer(msecs: Int)

    fun onShowMessage(text: String)
    fun onShowImage(path: String)
    fun onInputBox(prompt: String): String
    fun onShowMenu(items: List<RawItem>): Int
    fun onShowWindow(type: WindowType, isVisible: Boolean)

    fun onOpenGame(path: String, isGame: Boolean)
    fun onSaveGame(path: String)
    fun onLoadSave(path: String)

    fun onCheckAudioPlaying(path: String): Boolean
    fun onPlayAudio(path: String, volume: Int)
    fun onCloseAudio(path: String?)
}
