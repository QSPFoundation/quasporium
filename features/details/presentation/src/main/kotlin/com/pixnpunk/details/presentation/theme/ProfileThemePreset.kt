package com.pixnpunk.details.presentation.theme

enum class ProfileThemePreset(
    val title: String,
    val seedColor: Long
) {
    CORAL(
        title = "Crimson Ember",
        seedColor = 0xFFD9534F
    ),
    TEAL(
        title = "Nordic Wave",
        seedColor = 0xFF3B9E9B
    ),
    AMETHYST(
        title = "Twilight Mist",
        seedColor = 0xFF8E6FB9
    ),
    AMBER(
        title = "Autumn Sun",
        seedColor = 0xFFD48B38
    )
}
