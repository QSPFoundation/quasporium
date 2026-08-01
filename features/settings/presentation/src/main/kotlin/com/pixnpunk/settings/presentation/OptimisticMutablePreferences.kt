package com.pixnpunk.settings.presentation

import me.zhanghai.compose.preference.MutablePreferences
import me.zhanghai.compose.preference.Preferences

class OptimisticMutablePreferences(
    private val map: MutableMap<String, Any>,
    private val onEdit: (key: String, value: Any?) -> Unit
) : MutablePreferences {

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? = map[key] as T?

    override fun asMap(): Map<String, Any> = map

    override fun toMutablePreferences(): MutablePreferences = this

    override fun <T> set(key: String, value: T?) {
        if (value != null) {
            map[key] = value
        } else {
            map.remove(key)
        }
        onEdit(key, value)
    }

    override fun clear() {}

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Preferences) return false
        return map == other.asMap()
    }

    override fun hashCode(): Int = map.hashCode()
}
