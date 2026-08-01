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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Preferences) return false
        return stateMap == other.asMap()
    }

    override fun hashCode(): Int = stateMap.hashCode()
}
