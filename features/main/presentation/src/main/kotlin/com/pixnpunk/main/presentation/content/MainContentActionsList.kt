package com.pixnpunk.main.presentation.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.FlexJustifyContent
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import com.pixnpunk.main.presentation.model.MainUIState
import com.pixnpunk.main.presentation.model.ParsedActionItem

@OptIn(ExperimentalFlexBoxApi::class)
@Composable
fun MainContentActionsList(
    countActsVis: Int,
    uiState: MainUIState,
    fontFamily: FontFamily,
    actions: List<ParsedActionItem>,
    onActionClicked: (Int) -> Unit,
    scrollState: ScrollState
) {
    val textStyle = MaterialTheme.typography.bodyLarge.merge(
        TextStyle(
            color = Color(uiState.textColor),
            fontSize = uiState.fontSize.sp,
            fontFamily = fontFamily,
            background = Color(uiState.backColor)
        )
    )

    val density = LocalDensity.current
    val maxGridHeight = remember(countActsVis, density, textStyle.lineHeight) {
        calculateMaxActionsHeight(countActsVis, density, textStyle)
    }

    AnimatedContent(
        targetState = actions,
        transitionSpec = {
            (fadeIn(animationSpec = tween(300)) + slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { fullHeight -> fullHeight / 4 }
            )).togetherWith(fadeOut(animationSpec = tween(200)))
        },
        label = "Actions_Crossfade_Animation"
    ) { animatedActions ->
        FlexBox(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (countActsVis > 0) Modifier.heightIn(max = maxGridHeight)
                        else Modifier
                    )
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    .verticalScroll(scrollState)
                    .padding(16.dp),
            config = {
                direction(FlexDirection.Row)
                wrap(FlexWrap.Wrap)
                gap(8.dp)
                justifyContent(FlexJustifyContent.Start)
            }
        ) {
            animatedActions.forEachIndexed { index, item ->
                key(item.original.title) {
                    ActionCell(
                        modifier =
                            Modifier
                                .flex { basis(140.dp); grow(1f) }
                                .widthIn(min = 140.dp),
                        item = item,
                        textStyle = textStyle,
                        cardBackgroundColor = Color(uiState.backColor),
                        onItemClick = { onActionClicked(index) }
                    )
                }
            }
        }
    }
}

private fun calculateMaxActionsHeight(
    visibleRows: Int,
    density: Density,
    textStyle: TextStyle
): Dp = with(density) {
    if (visibleRows <= 0) return 0.dp

    val approxLineHeightPx = textStyle.lineHeight.takeOrElse { 24.sp }.toPx()
    val verticalPaddingPx = 32.dp.toPx()
    val singleRowHeightPx = approxLineHeightPx + verticalPaddingPx
    val spacingPx = 8.dp.toPx()
    val containerPaddingPx = 32.dp.toPx()

    val totalHeightPx =
        (singleRowHeightPx * visibleRows) + (spacingPx * (visibleRows - 1)) + containerPaddingPx
    totalHeightPx.toDp()
}