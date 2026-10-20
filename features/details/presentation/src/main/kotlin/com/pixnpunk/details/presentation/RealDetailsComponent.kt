package com.pixnpunk.details.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.essenty.statekeeper.saveable
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.details.presentation.mapper.withRelativeDates
import com.pixnpunk.details.presentation.mvi.DetailsStore
import com.pixnpunk.details.presentation.mvi.RealDetailsStore
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.LaunchGameEntity
import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.ocpsoft.prettytime.PrettyTime

class RealDetailsComponent(
    private val componentContext: ComponentContext,
    private val defEntry: GameEntry,
    override val settingsState: StateFlow<SettingsEntity>,
    private val launchBackPicker: () -> Unit,
    override val launchFreshGame: (LaunchGameEntity) -> Unit,
    override val launchContinueGame: (LaunchGameEntity) -> Unit,
    override val showEditGameDialog: (GameEntry) -> Unit,
    override val showSettingsGamePanel: (GameEntry) -> Unit,
    override val showDownloadGameDialog: (GameEntry) -> Unit,
    override val onDismissed: () -> Unit
) : ComponentContext by componentContext, DetailsComponent, KoinComponent {

    private val store = instanceKeeper.getStore {
        RealDetailsStore(DefaultStoreFactory(), defEntry).create()
    }

    private val prettyTime = PrettyTime()

    @OptIn(ExperimentalStateKeeperApi::class)
    private var config: Config by saveable(
        serializer = Config.serializer(),
        init = ::Config
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<DetailsStore.State> = store.stateFlow
    override val label: Flow<DetailsStore.Label> = store.labels

    override val displayEntry: StateFlow<GameEntry> =
        model.map { it.gameEntry.withRelativeDates(prettyTime) }
            .stateIn(
                scope = coroutineScope(),
                started = SharingStarted.Eagerly,
                initialValue = model.value.gameEntry.withRelativeDates(prettyTime)
            )

    override fun onCreateProfile() =
        store.accept(DetailsStore.Intent.CreateProfile)

    override fun onUpdateVisuals(presetId: Long, newName: String, newColor: Long) =
        store.accept(DetailsStore.Intent.UpdateProfileVisuals(presetId, newName, newColor))

    fun onSetBackground(uriString: String) {
        store.accept(
            DetailsStore.Intent.SetBackgroundImage(
                config.presetId ?: return,
                uriString
            )
        )
    }

    override fun onPickBackground(presetId: Long) {
        config = Config(presetId)
        launchBackPicker()
    }

    override fun onRemoveBackground(presetId: Long) =
        store.accept(DetailsStore.Intent.RemoveBackgroundImage(presetId))

    override fun onDeleteProfile(presetId: Long) =
        store.accept(DetailsStore.Intent.DeleteProfile(presetId))

    override fun onStartFresh(presetId: Long, fileName: String) =
        store.accept(DetailsStore.Intent.StartFresh(presetId, fileName))

    override fun onContinueSave(presetId: Long, savePath: String) =
        store.accept(DetailsStore.Intent.ContinueGame(presetId, savePath))

    @Serializable
    private data class Config(val presetId: Long? = null)
}
