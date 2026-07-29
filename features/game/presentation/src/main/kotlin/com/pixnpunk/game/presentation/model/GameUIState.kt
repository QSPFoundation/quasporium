package com.pixnpunk.game.presentation.model

import androidx.compose.runtime.Immutable
import com.pixnpunk.game.domain.model.GameId
import com.pixnpunk.game.domain.model.VfsPath

@Immutable
data class GameUIState(
    val isObjectsEnabled: Boolean = true,
    val isExtraEnabled: Boolean = true,
    val isInputEnabled: Boolean = true,
    val isExecutorEnabled: Boolean = false,

    val isSaveDisable: Boolean = false,
    val isUserChooseSave: Boolean = false,
    val isUserChooseLoad: Boolean = false,

    val gameId: GameId = GameId(-1),
    val gameTitle: String = "Title",
    val gameDirUriBox: VfsPath = VfsPath(""),
    val gameFileUriBox: VfsPath = VfsPath("")
)
