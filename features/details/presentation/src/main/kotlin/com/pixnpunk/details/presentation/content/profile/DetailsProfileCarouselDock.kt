package com.pixnpunk.details.presentation.content.profile

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import com.pixnpunk.details.presentation.model.ProfileUIState
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsProfileCarouselDock(
    profiles: List<ProfileUIState>,
    files: List<GameFileItem>,
    onCreateProfile: () -> Unit,
    onUpdateVisuals: (profileId: Long, newName: String, newColor: Long) -> Unit,
    onPickBackground: (profileId: Long) -> Unit,
    onDeleteProfile: (profileId: Long) -> Unit,
    onContinue: (profileId: Long, savePath: String) -> Unit,
    onStartFresh: (profileId: Long, fileName: String) -> Unit
) {
    val pageCount = profiles.size + 1
    val pagerState = rememberPagerState { pageCount }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (profiles.isNotEmpty()) {
            ProfileDockStation(
                modifier = Modifier.padding(horizontal = 8.dp),
                profiles = profiles,
                selectedPageIndex = pagerState.targetPage,
                onSelectPage = { index ->
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                },
                onAddClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(profiles.size) }
                }
            )
        }

        HorizontalPager(
            modifier = Modifier.fillMaxWidth(),
            state = pagerState,
            pageSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 8.dp),
            verticalAlignment = Alignment.Top
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
                if (page >= profiles.size) {
                    CreateProfileCard(
                        onClick = { if (isCurrentPage) onCreateProfile() }
                    )
                } else {
                    val profile = profiles[page]
                    PlayProfileCard(
                        profile = profile,
                        allFiles = files,
                        isCurrentPage = isCurrentPage,
                        onContinue = { saveName ->
                            if (isCurrentPage) onContinue(
                                profile.id,
                                saveName
                            )
                        },
                        onStartFresh = { fileName ->
                            if (isCurrentPage) onStartFresh(
                                profile.id,
                                fileName
                            )
                        },
                        onSaveVisuals = { newName, newColor ->
                            onUpdateVisuals(
                                profile.id,
                                newName,
                                newColor
                            )
                        },
                        onPickBackground = { onPickBackground(profile.id) },
                        onDeleteProfile = { onDeleteProfile(profile.id) }
                    )
                }
            }
        }
    }
}