package com.pixnpunk.settings.presentation

enum class AppLanguage(val tag: String) {
    SYSTEM(""),
    ENGLISH("en"),
    RUSSIAN("ru");

    companion object {
        fun fromLanguageTags(tags: String): AppLanguage {
            val language = tags.substringBefore(',').substringBefore('-')
            return entries.firstOrNull {
                it != SYSTEM && it.tag.equals(language, ignoreCase = true)
            } ?: SYSTEM
        }
    }
}
