package com.pixnpunk.details.presentation.content.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.details.presentation.model.ProfileUIState
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsProfileCarousel(
    profiles: List<ProfileUIState>,
    files: List<GameFileItem>,
    onCreateProfile: () -> Unit,
    onContinue: (profileId: Long, savePath: String) -> Unit,
    onStartFresh: (profileId: Long, fileName: String) -> Unit
) {
    val pageCount = profiles.size + 1
    val pagerState = rememberPagerState { pageCount }

    HorizontalPager(
        modifier = Modifier.fillMaxWidth(),
        state = pagerState,
        pageSize = PageSize.Fixed(310.dp),
        pageSpacing = 12.dp,
        contentPadding = PaddingValues(horizontal = 20.dp),
        verticalAlignment = Alignment.Top,
    ) { page ->
        val isCurrentPage = pagerState.settledPage == page

        Box(
            modifier = Modifier.graphicsLayer {
                val pageOffset =
                    ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                val scale = 1f - (pageOffset.coerceIn(0f, 1f) * 0.1f)
                scaleX = scale
                scaleY = scale
                alpha = 1f - (pageOffset.coerceIn(0f, 1f) * 0.4f)
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
                    onRenameProfile = { },
                    onDeleteProfile = { }
                )
            }
        }
    }
}