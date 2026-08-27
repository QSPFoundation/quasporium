package com.pixnpunk.stock.presentation.model

import androidx.compose.runtime.Immutable
import com.pixnpunk.stock.domain.model.SearchTokenType
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class SearchTokenUI(
    val value: String,
    val type: SearchTokenType,
    val displayText: String
)
