package com.pixnpunk.game.domain.model

sealed interface EngineDialog {
    data class Message(val text: String) : EngineDialog
    data class Input(val prompt: String) : EngineDialog
    data class Picture(val imagePath: VfsPath) : EngineDialog
    data class Menu(val items: List<InteractableItem>) : EngineDialog
    data class Error(val details: String) : EngineDialog
}
