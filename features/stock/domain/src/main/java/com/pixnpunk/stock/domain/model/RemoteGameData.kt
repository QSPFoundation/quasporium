package com.pixnpunk.stock.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlCData

@Serializable
@SerialName("game")
data class RemoteGameData(
    @XmlCData
    val id: Long = 0,
    @XmlCData
    @SerialName("list_id")
    val listId: Int = 1,
    @XmlCData
    val author: String = "",
    @XmlCData
    @SerialName("ported_by")
    val portedBy: String = "",
    @XmlCData
    val version: String = "",
    @XmlCData
    val title: String = "",
    @XmlCData
    val lang: String = "",
    @XmlCData
    val player: String = "",
    @XmlCData
    val icon: String = "",
    @XmlCData
    @SerialName("file_url")
    val fileUrl: String = "",
    @XmlCData
    @SerialName("file_size")
    val fileSize: Long = 0,
    @XmlCData
    @SerialName("file_ext")
    val fileExt: String = "",
    @XmlCData
    @SerialName("desc_url")
    val descUrl: String = "",
    @XmlCData
    @SerialName("pub_date")
    val pubDate: String = "",
    @XmlCData
    @SerialName("mod_date")
    val modDate: String = ""
)