package com.pixnpunk.quasporium

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanels
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanelsAnimators
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.details.presentation.DetailsContent
import com.pixnpunk.dialogs.presentation.DialogsMainContent
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.InputState
import com.pixnpunk.quasporium.mvi.RootStore
import com.pixnpunk.quasporium.theme.QuasporiumTheme
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.settings.presentation.SettingsContent
import com.pixnpunk.stock.presentation.StockContent
import me.zhanghai.compose.preference.LocalPreferenceFlow
import me.zhanghai.compose.preference.ProvidePreferenceLocals

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootContent(
    component: RootComponent,
    settingsRepository: SettingsRepository
) {
    val dialogSlot by component.dialogSlot.subscribeAsState()

    ProvidePreferenceLocals {
        val pref by LocalPreferenceFlow.current.collectAsState()
        LaunchedEffect(Unit) {
            component.label.collect {
                when (it) {
                    is RootStore.Label.ColorConfig -> {
                        settingsRepository.emitColorValue(it.state, it.color)
                        when (it.state) {
                            ColorPickerState.COLOR_TEXT ->
                                pref.toMutablePreferences()["textColor"] = it.color

                            ColorPickerState.COLOR_BACK ->
                                pref.toMutablePreferences()["backColor"] = it.color

                            ColorPickerState.COLOR_LINK ->
                                pref.toMutablePreferences()["linkColor"] = it.color
                        }
                    }

                    is RootStore.Label.ImageConfig -> {
                        settingsRepository.emitImageConf(it.state, it.config)
                        when (it.state) {
                            InputState.INPUT_IMAGE_WIDTH ->
                                pref.toMutablePreferences()["manualImageWidthPref"] = it.config

                            InputState.INPUT_IMAGE_HEIGHT ->
                                pref.toMutablePreferences()["manualImageHeightPref"] = it.config

                            else -> {}
                        }
                    }
                }
            }
        }
        settingsRepository.emitMap(pref.asMap())
    }

    val settings by settingsRepository.settingsFlow.collectAsState()

    QuasporiumTheme(
        darkTheme =
            when {
                settings.theme.contains("auto") -> isSystemInDarkTheme()
                settings.theme.contains("dark") -> true
                settings.theme.contains("light") -> false
                else -> false
            }
    ) {
        dialogSlot.child?.instance?.also {
            DialogsMainContent(it, settingsRepository)
        }

        ChildPanels(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            panels = component.childPanel,
            mainChild = { StockContent(it.instance, settings) },
            detailsChild = { DetailsContent(it.instance, settings) },
            extraChild = { SettingsContent(it.instance, settingsRepository) },
            animators = ChildPanelsAnimators(single = fade() + scale(), dual = fade() to fade()),
//            predictiveBackParams = { // See the docs below
//                PredictiveBackParams(
//                    backHandler = component.backHandler,
//                    onBack = component::onBackClicked,
//                    animatable = ::materialPredictiveBackAnimatable,
//                )
//            },
        )
    }
}