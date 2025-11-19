package com.pixnpunk.dto

import kotlinx.serialization.Serializable

@Serializable
data class LibGameState(
    val gameRunning: Boolean = false,
    val gameId: Long = 0L,
    val gameTitle: String = "",
    val gameDirPath: String = "",
    val gameFilePath: String = "",
    val mainDesc: String = "",
    val varsDesc: String = "",
    val actionsList: List<LibGenItem> = listOf(),
    val objectsList: List<LibGenItem> = listOf(),
)