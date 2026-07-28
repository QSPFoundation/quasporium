package com.pixnpunk.settings.presentation

import me.zhanghai.compose.preference.MutablePreferences

class ProxyMutablePreferences(
    private val stateMap: Map<String, Any>,
    private val onEdit: (key: String, value: Any?) -> Unit,
    private val onClear: () -> Unit
) : MutablePreferences {

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? = stateMap[key] as T?

    override fun asMap(): Map<String, Any> = stateMap

    override fun toMutablePreferences(): MutablePreferences = this

    override fun <T> set(key: String, value: T?) = onEdit(key, value)

    override fun clear() = onClear()
}
