package com.pixnpunk.details.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.details.presentation.mvi.DetailsStore
import com.pixnpunk.details.presentation.mvi.RealDetailsStore
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.SettingsEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.ocpsoft.prettytime.PrettyTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class RealDetailsComponent(
    private val componentContext: ComponentContext,
    private val defEntry: GameEntry,
    override val settingsState: StateFlow<SettingsEntity>,
    override val showSelectGameDialog: (GameEntry) -> Unit,
    override val showEditGameDialog: (GameEntry) -> Unit,
    override val showSettingsGamePanel: (GameEntry) -> Unit,
    override val showDownloadGameDialog: (GameEntry) -> Unit,
    override val onDismissed: () -> Unit
) : ComponentContext by componentContext, DetailsComponent, KoinComponent {

    private val store = instanceKeeper.getStore {
        RealDetailsStore(DefaultStoreFactory(), defEntry).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<DetailsStore.State> = store.stateFlow

    override fun convEntryData(
        entry: GameEntry
    ): GameEntry {
        if (entry.pubDate.isEmpty() || entry.modDate.isEmpty()) return entry

        val formatPubDate =
            runCatching {
                OffsetDateTime.parse(entry.pubDate).toInstant()
            }.onFailure {
                Instant.parse(
                    LocalDateTime.parse(
                        entry.pubDate,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    ).toInstant(ZoneOffset.UTC).toString()
                )
            }

        val formatModDate =
            runCatching {
                OffsetDateTime.parse(entry.pubDate).toInstant()
            }.onFailure {
                Instant.parse(
                    LocalDateTime.parse(
                        entry.modDate,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    ).toInstant(ZoneOffset.UTC).toString()
                )
            }

        return entry.copy(
            pubDate =
                if (formatPubDate.isSuccess) PrettyTime().format(formatModDate.getOrNull())
                else entry.pubDate,
            modDate =
                if (formatModDate.isSuccess) PrettyTime().format(formatModDate.getOrNull())
                else entry.modDate
        )
    }
}