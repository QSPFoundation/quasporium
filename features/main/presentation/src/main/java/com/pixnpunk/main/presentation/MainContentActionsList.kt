package com.pixnpunk.main.presentation

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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalFlexBoxApi
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexDirection
import androidx.compose.foundation.layout.FlexJustifyContent
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.takeOrElse
import coil3.compose.AsyncImage
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.main.presentation.model.ParsedActionItem

@OptIn(ExperimentalFlexBoxApi::class)
@Composable
fun MainContentActionsList(
    countActsVis: Int,
    settings: SettingsEntity,
    fontFamily: FontFamily,
    actions: List<ParsedActionItem>,
    onActionClicked: (Int) -> Unit,
    scrollState: ScrollState
) {
    val textStyle = MaterialTheme.typography.bodyLarge.merge(
        TextStyle(
            color = Color(settings.textColor),
            fontSize = settings.fontSize.sp,
            fontFamily = fontFamily,
            background = Color(settings.backColor)
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
                key(item.original.text + index) {
                    ActionCell(
                        modifier =
                            Modifier
                                .flex { grow(1f) }
                                .widthIn(min = 140.dp),
                        item = item,
                        textStyle = textStyle,
                        cardBackgroundColor = Color(settings.backColor),
                        onItemClick = { onActionClicked(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCell(
    modifier: Modifier = Modifier,
    item: ParsedActionItem,
    textStyle: TextStyle,
    cardBackgroundColor: Color,
    onItemClick: () -> Unit
) {
    Card(
        onClick = onItemClick,
        modifier = modifier
            .widthIn(min = 140.dp)
            .clearAndSetSemantics { contentDescription = item.annotatedText.text },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (item.hasImage) {
                AsyncImage(
                    model = item.original.imagePath,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            }

            Text(
                text = item.annotatedText,
                style = textStyle,
                modifier = Modifier.weight(1f)
            )
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
