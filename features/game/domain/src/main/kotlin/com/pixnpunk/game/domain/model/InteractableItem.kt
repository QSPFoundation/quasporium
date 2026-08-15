package com.pixnpunk.game.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InteractableItem(
    val title: String,
    val imagePath: VfsPath? = null
)
