package com.pixnpunk.stock.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenericApiResponse<T>(
    val data: List<T>,
    val meta: Meta
) {
    @Serializable
    data class Meta(
        @SerialName("current_page") val currentPage: Int,
        @SerialName("last_page") val lastPage: Int
    )
}