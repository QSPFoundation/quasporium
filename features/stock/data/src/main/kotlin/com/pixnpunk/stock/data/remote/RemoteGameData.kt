package com.pixnpunk.stock.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("data")
data class RemoteGameData(
    @Serializable(with = SlugAsIdSerializer::class)
    @SerialName("slug")
    val id: Long = 0,
    val name: String,
    val lang: String? = null,
    val ver: String,
    val authors: String,
    val translators: String? = null,
    @SerialName("description_html") val descriptionHtml: String,
    @SerialName("file_size") val fileSize: Long? = 0,
    @SerialName("file_name") val fileName: String? = null,
    @SerialName("icon_url") val iconUrl: String? = null,
    @SerialName("cover_url") val coverUrl: String? = null,
    @SerialName("file_url") val fileUrl: String,
    @SerialName("downloads_count") val downloadsCount: Int,
    @SerialName("plays_count") val playsCount: Int,
    @SerialName("likes_count") val likesCount: Int,
    @SerialName("comments_count") val commentsCount: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)
