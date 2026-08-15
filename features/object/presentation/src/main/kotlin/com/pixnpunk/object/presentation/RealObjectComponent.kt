package com.pixnpunk.`object`.presentation

import android.graphics.Typeface
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.fromHtml
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.domain.model.GameState
import com.pixnpunk.game.domain.repository.EngineCommand
import com.pixnpunk.game.domain.repository.GameEngineInteractor
import com.pixnpunk.`object`.presentation.model.ObjectUIState
import com.pixnpunk.`object`.presentation.model.ParsedObjectItem
import com.pixnpunk.utils.ViewUtil.toComposeFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import java.io.File

class RealObjectComponent(
    private val componentContext: ComponentContext,
    private val engineInteractor: GameEngineInteractor,
    private val settingsState: StateFlow<SettingsEntity>
) : ComponentContext by componentContext, ObjectComponent, KoinComponent {

    override val onObjectSelected: (Int) -> Unit = { index ->
        engineInteractor.submitCommand(EngineCommand.SelectObject(index))
    }

    private data class CachedSettings(
        val raw: SettingsEntity,
        val fontFamily: FontFamily
    )

    private val cachedSettingsFlow: Flow<CachedSettings> = settingsState
        .map { settings ->
            CachedSettings(
                raw = settings,
                fontFamily = resolveFontFamily(settings)
            )
        }
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()

    override val uiState: StateFlow<ObjectUIState> = combine(
        engineInteractor.state,
        cachedSettingsFlow
    ) { gameState, processed ->
        if (gameState !is GameState.Running) return@combine ObjectUIState()

        val settings = processed.raw
        val libConfig = gameState.uiConfig

        ObjectUIState(
            parsedObjects = gameState.objects.map { item ->
                ParsedObjectItem(
                    original = item,
                    annotatedText = AnnotatedString.fromHtml(item.title)
                )
            },
            backColor =
                libConfig.backColor
                    .takeIf { !settings.isBackColorOverride && it != 0 }
                    ?: settings.backColor,
            fontSize =
                libConfig.fontSize
                    .takeIf { settings.isUseGameFont && it != 0 }
                    ?: settings.fontSize,
            fontFamily = processed.fontFamily
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = coroutineScope(),
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ObjectUIState()
    )

    private fun resolveFontFamily(settings: SettingsEntity): FontFamily {
        if (settings.isUseCustomFont && settings.customFontPath.isNotEmpty()) {
            val file = File(settings.customFontPath)
            if (file.exists()) {
                return runCatching { FontFamily(Typeface.createFromFile(file)) }
                    .getOrDefault(settings.typeface.toComposeFamily())
            }
        }
        return settings.typeface.toComposeFamily()
    }
}
