package com.pixnpunk.settings.presentation

import me.zhanghai.compose.preference.MutablePreferences

class OptimisticMutablePreferences(
    private val map: MutableMap<String, Any>,
    private val onEdit: (key: String, value: Any?) -> Unit
) : MutablePreferences {

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? = map[key] as T?

    override fun asMap(): Map<String, Any> = map

    override fun toMutablePreferences(): MutablePreferences =
        OptimisticMutablePreferences(map.toMutableMap(), onEdit)

    override fun <T> set(key: String, value: T?) {
        if (value != null) {
            map[key] = value
        } else {
            map.remove(key)
        }
        onEdit(key, value)
    }

    override fun clear() = map.clear()
}
