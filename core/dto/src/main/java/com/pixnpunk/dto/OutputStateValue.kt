package com.pixnpunk.dto

data class OutputStateValue(
    val state: OutputState,
    val menuIndex: Int = -1,
    val selFileBox: String = ""
)
