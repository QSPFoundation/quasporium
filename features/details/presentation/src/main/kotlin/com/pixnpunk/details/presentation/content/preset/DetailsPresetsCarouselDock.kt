package com.pixnpunk.details.presentation.content.preset

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.details.presentation.model.PresetUIState
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsPresetsCarouselDock(
    presets: List<PresetUIState>,
    files: List<GameFileItem>,
    onCreateProfile: () -> Unit,
    onUpdateVisuals: (profileId: Long, newName: String, newColor: Long) -> Unit,
    onPickBackground: (profileId: Long) -> Unit,
    onRemoveBackground: (profileId: Long) -> Unit,
    onDeleteProfile: (profileId: Long) -> Unit,
    onContinue: (profileId: Long, savePath: String) -> Unit,
    onStartFresh: (profileId: Long, fileName: String) -> Unit
) {
    val pageCount = presets.size + 1
    val pagerState = rememberPagerState { pageCount }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = presets.isNotEmpty()
        ) {
            PresetDockStation(
                modifier = Modifier.padding(horizontal = 8.dp),
                presets = presets,
                selectedPageIndex = pagerState.targetPage,
                onSelectPage = { index ->
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                },
                onAddClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(presets.size) }
                }
            )
        }

        HorizontalPager(
            modifier = Modifier.fillMaxWidth(),
            state = pagerState,
            pageSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 8.dp),
            verticalAlignment = Alignment.Top,
            key = { page -> if (page < presets.size) presets[page].id else "create_preset_card" }
        ) { page ->
            val isCurrentPage = pagerState.settledPage == page

            Box(
                modifier = Modifier.graphicsLayer {
                    ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                        .absoluteValue
                        .also { pageOffset ->
                            scaleX = 1f - (pageOffset.coerceIn(0f, 1f) * 0.05f)
                            scaleY = 1f - (pageOffset.coerceIn(0f, 1f) * 0.05f)
                            alpha = 1f - (pageOffset.coerceIn(0f, 1f) * 0.35f)
                        }
                }
            ) {
                if (page >= presets.size) {
                    CreatePresetCard(
                        onClick = { if (isCurrentPage) onCreateProfile() }
                    )
                } else {
                    val profile = presets[page]
                    PlayPresetCard(
                        profile = profile,
                        allFiles = files,
                        onContinue = { saveName ->
                            if (isCurrentPage) onContinue(profile.id, saveName)
                        },
                        onStartFresh = { fileName ->
                            if (isCurrentPage) onStartFresh(profile.id, fileName)
                        },
                        onSaveVisuals = { newName, newColor ->
                            onUpdateVisuals(profile.id, newName, newColor)
                        },
                        onPickBackground = { onPickBackground(profile.id) },
                        onRemoveBackground = { onRemoveBackground(profile.id) },
                        onDeleteProfile = { onDeleteProfile(profile.id) }
                    )
                }
            }
        }
    }
}