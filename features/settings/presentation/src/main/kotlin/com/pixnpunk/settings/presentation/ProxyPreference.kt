package com.pixnpunk.settings.presentation

import me.zhanghai.compose.preference.MutablePreferences
import me.zhanghai.compose.preference.Preferences

class ProxyPreference(
    private val stateMap: Map<String, Any>,
    private val onEdit: (key: String, value: Any?) -> Unit
) : Preferences {

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? = stateMap[key] as T?

    override fun asMap(): Map<String, Any> = stateMap

    override fun toMutablePreferences(): MutablePreferences =
        OptimisticMutablePreferences(stateMap.toMutableMap(), onEdit)
}
