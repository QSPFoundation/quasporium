package com.pixnpunk.game.domain.repository

import com.pixnpunk.game.domain.model.VfsPath

sealed interface EngineCommand {
    data class ExecuteAction(val index: Int) : EngineCommand
    data class ExecuteString(val input: String) : EngineCommand
    data class SelectObject(val index: Int) : EngineCommand
    data class SubmitInput(val text: String) : EngineCommand
    data class SubmitInputArea(val text: String) : EngineCommand
    data class SelectMenuItem(val index: Int) : EngineCommand
    data class LoadGame(val path: VfsPath) : EngineCommand
    data class LoadSave(val path: VfsPath) : EngineCommand
    data class SaveGame(val path: VfsPath) : EngineCommand
    data object RestartGame : EngineCommand
    data object DismissDialog : EngineCommand
}
