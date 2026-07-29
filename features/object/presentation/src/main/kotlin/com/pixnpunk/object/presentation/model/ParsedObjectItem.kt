package com.pixnpunk.`object`.presentation.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.pixnpunk.game.domain.model.InteractableItem

@Immutable
data class ParsedObjectItem(
    val original: InteractableItem,
    val annotatedText: AnnotatedString = AnnotatedString(""),
    val hasImage: Boolean = false
)
