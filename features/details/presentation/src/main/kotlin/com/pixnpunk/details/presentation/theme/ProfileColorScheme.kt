package com.pixnpunk.details.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

@Immutable
data class ProfileColorScheme(
    val cardContainer: Color,
    val onCardContainer: Color,
    val primaryButton: Color,
    val onPrimaryButton: Color,
    val secondaryButton: Color,
    val onSecondaryButton: Color,
    val sheetContainer: Color,
    val onSheetContainer: Color
)

val LocalProfileColors = staticCompositionLocalOf<ProfileColorScheme> {
    error("ProfileColorScheme not provided")
}

@Composable
fun rememberProfileColorScheme(
    seedColor: Color,
    isDark: Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f
): ProfileColorScheme {
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    return remember(seedColor, isDark, surface, onSurface) {
        if (isDark) {
            ProfileColorScheme(
                cardContainer = seedColor.copy(alpha = 0.18f).compositeOver(surface),
                onCardContainer = seedColor.copy(alpha = 0.95f),
                primaryButton = seedColor,
                onPrimaryButton = if (seedColor.luminance() > 0.4f) Color.Black else Color.White,
                secondaryButton = seedColor.copy(alpha = 0.25f).compositeOver(surface),
                onSecondaryButton = seedColor.copy(alpha = 0.9f),
                sheetContainer = Color(0xFF1E1E1E),
                onSheetContainer = Color.White.copy(alpha = 0.9f)
            )
        } else {
            ProfileColorScheme(
                cardContainer = seedColor.copy(alpha = 0.12f).compositeOver(surface),
                onCardContainer = onSurface,
                primaryButton = seedColor,
                onPrimaryButton = if (seedColor.luminance() > 0.4f) Color.Black else Color.White,
                secondaryButton = seedColor.copy(alpha = 0.15f).compositeOver(surface),
                onSecondaryButton = onSurface.copy(alpha = 0.85f),
                sheetContainer = Color(0xFFF5F5F5),
                onSheetContainer = onSurface
            )
        }
    }
}