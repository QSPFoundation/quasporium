package com.pixnpunk.game.presentation.mvi

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.utils.DirUtil.isGameDirIsInternal
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealGameStore(
    private val storeFactory: StoreFactory,
    private val settingsState: StateFlow<SettingsEntity>,
    private val gameId: Long,
    private val gameTitle: String,
    private val gameDirUriBox: String,
    private val gameFileUriBox: String
) : KoinComponent {
    internal fun create(): GameStore = GameImpl()

    private inner class GameImpl : GameStore,
        Store<GameStore.Intent, GameStore.State, GameStore.Label> by storeFactory.create(
            name = "RootStore",
            initialState = GameStore.State(
                gameId = gameId,
                gameTitle = gameTitle,
                gameDirUriBox = gameDirUriBox,
                gameFileUriBox = gameFileUriBox,
                isGameDirInternal = isGameDirIsInternal(gameDirUriBox),
            ),
            bootstrapper = SimpleBootstrapper(
                GameStore.Action.StartGameStateFlow,
                GameStore.Action.StartGamePopupFlow,
                GameStore.Action.StartGameElementVisFlow,
                GameStore.Action.StartGameDialogFlow
            ),
            executorFactory = {
                GameExecutor(
                    get(), get(),
                    settingsState, get()
                )
            },
            reducer = GameReducer
        )
}