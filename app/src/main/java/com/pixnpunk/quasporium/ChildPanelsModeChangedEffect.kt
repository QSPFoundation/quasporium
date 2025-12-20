package com.pixnpunk.quasporium

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.window.core.layout.WindowSizeClass
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun ChildPanelsModeChangedEffect(onModeChanged: (ChildPanelsMode) -> Unit) {
    val isScreenExpanded = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    val mode =
        if (isScreenExpanded) ChildPanelsMode.DUAL
        else ChildPanelsMode.SINGLE

    DisposableEffect(mode) {
        onModeChanged(mode)
        onDispose {}
    }
}