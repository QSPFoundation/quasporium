package com.pixnpunk.database

import androidx.room.TypeConverter

class ListStringConverter {
    @TypeConverter
    fun fromListPath(gameFilesPath: List<String>): String {
        return gameFilesPath.joinToString()
    }

    @TypeConverter
    fun toListPath(data: String): List<String> {
        return data.split(", ")
    }
}