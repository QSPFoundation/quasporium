package com.pixnpunk.settings.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.dto.repository.SettingsRepository
import com.pixnpunk.settings.presentation.mappers.dispatch
import com.pixnpunk.settings.presentation.mappers.toMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.Preferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RealSettingsComponent(
    private val componentContext: ComponentContext,
    private val gameId: Long? = null,
    override val gameTitle: String = "",
    override val showColorDialog: (ColorPickerState) -> Unit,
    override val resetToDefault: (ColorPickerState) -> Unit,
    override val showInputDialog: (InputState) -> Unit,
    override val showFontDialog: () -> Unit,
    override val onDismissed: () -> Unit
) : ComponentContext by componentContext, SettingsComponent, KoinComponent {

    private val scope = coroutineScope()

    private val navigation = StackNavigation<ChildConfig>()

    private val settingsRepository: SettingsRepository by inject()

    override val isLocalSettings: Boolean = gameId != null

    override val preferencesState = MutableStateFlow<Preferences>(
        createProxyPreferences(SettingsEntity())
    )

    init {
        scope.launch {
            settingsRepository.settingsFlow.collect { entity ->
                preferencesState.value = createProxyPreferences(entity)
            }
        }
    }

    override fun moveToChild(config: ChildConfig) = navigation.pushNew(config)

    override fun backToRoot() = navigation.pop()

    override val childStack: Value<ChildStack<*, SettingsComponent.Child>> =
        childStack(
            source = navigation,
            serializer = null,
            initialConfiguration = ChildConfig.Root,
            handleBackButton = true,
            childFactory = { config, _ ->
                when (config) {
                    is ChildConfig.Audio -> SettingsComponent.Child.AudioChild()

                    is ChildConfig.General -> SettingsComponent.Child.GeneralChild()

                    is ChildConfig.Pictures -> SettingsComponent.Child.PicturesChild()

                    is ChildConfig.Root -> SettingsComponent.Child.RootChild()

                    is ChildConfig.Text -> SettingsComponent.Child.TextChild()
                }
            }
        )

    private fun createProxyPreferences(
        entity: SettingsEntity
    ) = ProxyMutablePreferences(
        stateMap = entity.toMap(),
        onEdit = ::handleUIEdit,
        onClear = { }
    )

    private fun handleUIEdit(key: String, value: Any?) {
        val tempMap = preferencesState.value.asMap().toMutableMap()
        if (value != null) tempMap[key] = value else tempMap.remove(key)
        preferencesState.value = ProxyMutablePreferences(
            tempMap,
            ::handleUIEdit,
            {}
        )

        if (value != null) {
            scope.launch { settingsRepository.dispatch(key, value) }
        }
    }

    @Serializable
    sealed class ChildConfig {
        @Serializable
        data object Root : ChildConfig()

        @Serializable
        data object General : ChildConfig()

        @Serializable
        data object Text : ChildConfig()

        @Serializable
        data object Pictures : ChildConfig()

        @Serializable
        data object Audio : ChildConfig()
    }
}
