package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.GameListType
import com.pixnpunk.stock.domain.usecase.ObserveEntryByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ocpsoft.prettytime.PrettyTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

class DetailsExecutor(
    private val observeEntryByIdUseCase: ObserveEntryByIdUseCase
) : CoroutineExecutor<DetailsStore.Intent, DetailsStore.Action, DetailsStore.State, DetailsStore.Message, DetailsStore.Label>() {

    override fun executeAction(action: DetailsStore.Action) {
        when (action) {
            is DetailsStore.Action.StartEntryFlow -> {
                if (action.entry.listId == GameListType.Downloadable) {
                    convEntryData(
                        entry = action.entry,
                        onEntryConverted = { entry ->
                            scope.launch { dispatch(DetailsStore.Message.UpdateEntry(entry)) }
                        }
                    )
                } else if (action.entry.listId == GameListType.Playable) {
                    scope.launch(Dispatchers.IO) {
                        observeEntryByIdUseCase.execute(action.entry.id).collect { entry ->
                            convEntryData(
                                entry = entry,
                                onEntryConverted = { entry ->
                                    scope.launch { dispatch(DetailsStore.Message.UpdateEntry(entry)) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun convEntryData(
        entry: GameEntry,
        onEntryConverted: (GameEntry) -> Unit
    ) {
        val formatPubDate = try {
            Instant.parse(entry.pubDate)
        } catch (_: DateTimeParseException) {
            Instant.parse(
                LocalDateTime.parse(
                    entry.pubDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                ).toInstant(ZoneOffset.UTC).toString()
            )
        }

        val formatModDate = try {
            Instant.parse(entry.modDate)
        } catch (_: DateTimeParseException) {
            Instant.parse(
                LocalDateTime.parse(
                    entry.modDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                ).toInstant(ZoneOffset.UTC).toString()
            )
        }

        onEntryConverted(
            entry.copy(
                pubDate = PrettyTime().format(formatPubDate),
                modDate = PrettyTime().format(formatModDate)
            )
        )
    }
}