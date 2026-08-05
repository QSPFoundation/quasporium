package com.pixnpunk.main.presentation.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.pixnpunk.game.domain.model.InteractableItem

@Immutable
data class ParsedActionItem(
    val original: InteractableItem,
    val annotatedText: AnnotatedString = AnnotatedString("")
)
