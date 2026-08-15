package com.pixnpunk.game.domain.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class VfsPath(val value: String) {
    init { require(value.isNotBlank()) { "VfsPath cannot be empty" } }
}
