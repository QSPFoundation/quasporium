package com.pixnpunk.main.presentation

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import coil3.compose.AsyncImage
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.main.presentation.model.ParsedActionItem
import com.pixnpunk.utils.ViewUtil.toComposeFamily
import java.io.File

@Composable
fun MainContentActionsList(
    countActsVis: Int,
    settings: SettingsEntity,
    actions: List<ParsedActionItem>,
    onActionClicked: (Int) -> Unit,
    gridState: LazyStaggeredGridState
) {
    val customFontFamily = remember(
        settings.isUseCustomFont,
        settings.customFontPath,
        settings.typeface
    ) {
        if (settings.isUseCustomFont && settings.customFontPath.isNotEmpty()) {
            File(settings.customFontPath).let {
                if (it.exists()) {
                    FontFamily(Typeface.createFromFile(it))
                } else {
                    settings.typeface.toComposeFamily()
                }
            }
        } else {
            settings.typeface.toComposeFamily()
        }
    }

    val textStyle = MaterialTheme.typography.bodyLarge.merge(
        TextStyle(
            color = Color(settings.textColor),
            fontSize = settings.fontSize.sp,
            fontFamily = customFontFamily,
            background = Color(settings.backColor)
        )
    )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columnCount = remember(maxWidth, maxHeight) {
            val boxWindowSizeClass =
                WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(
                    widthDp = maxWidth.value,
                    heightDp = maxHeight.value
                )

            when {
                boxWindowSizeClass.isWidthAtLeastBreakpoint(
                    WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
                ) -> 4

                boxWindowSizeClass.isWidthAtLeastBreakpoint(
                    WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
                ) -> 3

                else -> 2
            }
        }

        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()

        val actionsWithSpans = remember(actions, maxWidth, columnCount, textStyle, density) {
            val columnWidthPx = with(density) {
                val gridPadding = 32.dp // 16.dp * 2
                val innerSpacing = 8.dp * (columnCount - 1)
                (maxWidth - gridPadding - innerSpacing).toPx() / columnCount
            }
            val spans = calculateSpans(actions, columnCount, columnWidthPx, textMeasurer, textStyle, density)
            actions.mapIndexed { index, item ->
                ActionWithSpan(item, spans[index], index)
            }
        }

        LazyVerticalStaggeredGrid(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (countActsVis > 0) {
                        Modifier.heightIn(
                            max = calculateGridHeight(countActsVis, density)
                        )
                    } else Modifier
                ),
            columns = StaggeredGridCells.Fixed(columnCount),
            state = gridState,
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            items(
                items = actionsWithSpans,
                key = { it.item.original.text + it.originalIndex },
                span = { if (it.isFullLine) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane },
            ) { actionWithSpan ->
                GridCell(
                    modifier = Modifier.animateItem(),
                    item = actionWithSpan.item,
                    isFullLine = actionWithSpan.isFullLine,
                    onItemClick = { onActionClicked(actionWithSpan.originalIndex) },
                    textStyle = textStyle,
                    cardBackgroundColor = Color(settings.backColor)
                )
            }
        }
    }
}

@Preview
@Composable
private fun MainContentActionsListPreview() {
    val settings = SettingsEntity(
        textColor = 0xFFFFFFFF.toInt(),
        backColor = 0xFF000000.toInt(),
        linkColor = 0xFF00BFFF.toInt(),
        fontSize = 16,
        isUseGameFont = false,
    )
    val actions = listOf(
        LibGenItem(text = "Action 1", imagePath = ""),
        LibGenItem(text = "Action 2", imagePath = ""),
        LibGenItem(text = "Action 3", imagePath = ""),
        LibGenItem(text = "Looooooong Action 5", imagePath = "")
    ).map { ParsedActionItem(original = it) }
    MainContentActionsList(
        countActsVis = 3,
        settings = settings,
        actions = actions,
        onActionClicked = {},
        gridState = rememberLazyStaggeredGridState()
    )
}

private fun calculateSpans(
    items: List<ParsedActionItem>,
    columnCount: Int,
    columnWidthPx: Float,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    density: Density
): BooleanArray {
    if (columnCount <= 1) return booleanArrayOf(false)

    val spans = BooleanArray(items.size)
    val imageOverheadPx = with(density) { 104.dp.toPx() }
    val noImageOverheadPx = with(density) { 32.dp.toPx() }

    items.forEachIndexed { index, item ->
        val textWidth = textMeasurer.measure(
            text = item.annotatedText.text,
            style = textStyle,
            maxLines = 1,
            softWrap = false
        ).size.width

        val overheadPx =
            if (item.hasImage) imageOverheadPx
            else noImageOverheadPx

        spans[index] = (textWidth + overheadPx > columnWidthPx)
    }

    var groupStart = 0
    for (i in items.indices) {
        if (spans[i]) {
            for (j in groupStart until i) spans[j] = true
            groupStart = i + 1
        } else if (i - groupStart + 1 == columnCount) {
            groupStart = i + 1
        }
    }

    for (j in groupStart until items.size) spans[j] = true

    return spans
}

private fun calculateGridHeight(
    visibleRows: Int,
    density: Density,
    spacingDp: Dp = 8.dp,
    contentPaddingDp: Dp = 32.dp
): Dp = with(density) {
    if (visibleRows <= 0) return 0.dp
    (72 * fontScale).dp * visibleRows + spacingDp * (visibleRows - 1) + contentPaddingDp
}

private data class ActionWithSpan(
    val item: ParsedActionItem,
    val isFullLine: Boolean,
    val originalIndex: Int
)

@Composable
private fun GridCell(
    modifier: Modifier = Modifier,
    item: ParsedActionItem,
    isFullLine: Boolean,
    textStyle: TextStyle,
    cardBackgroundColor: Color,
    onItemClick: () -> Unit
) {
    Card(
        onClick = onItemClick,
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = item.annotatedText.text },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                maxLines = if (isFullLine) 3 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}