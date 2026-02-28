package com.pixnpunk.main.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.utils.ViewUtil.toTypeface
import kotlin.math.ceil

@Composable
fun MainContentActionsList(
    countActsVis: Int,
    settings: SettingsEntity,
    actions: List<LibGenItem>,
    onActionClicked: (Int) -> Unit,
    gridState: LazyStaggeredGridState
) {
    var baseItemHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val minColumnWidth = 160.dp
        val columnsCount = maxOf(1, (maxWidth / minColumnWidth).toInt())

        val estimatedRows = ceil(actions.size.toFloat() / columnsCount).toInt()
        val visibleRows = minOf(countActsVis, estimatedRows)
        val maxHeight = if (baseItemHeight > 0.dp) baseItemHeight * visibleRows else 1000.dp

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minColumnWidth),
            state = gridState,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
        ) {
            itemsIndexed(
                items = actions,
                span = { _, act ->
                    val cleanText = AnnotatedString.fromHtml(act.text).text
                    if (act.imagePath.isNotBlank() || cleanText.length > 35) {
                        StaggeredGridItemSpan.FullLine
                    } else {
                        StaggeredGridItemSpan.SingleLane
                    }
                }
            ) { key, act ->
                Column(
                    modifier = Modifier
                        .then(
                            if (baseItemHeight == 0.dp && act.imagePath.isBlank()) {
                                Modifier.onGloballyPositioned { crd ->
                                    baseItemHeight = with(density) { crd.size.height.toDp() }
                                }
                            } else Modifier
                        )
                ) {
                    ListItem(
                        modifier = Modifier.clickable { onActionClicked(key) },
                        colors = ListItemDefaults.colors(
                            containerColor = Color(settings.backColor)
                        ),
                        leadingContent = {
                            if (act.imagePath.isNotBlank()) {
                                AsyncImage(
                                    model = act.imagePath,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                text = AnnotatedString.fromHtml(
                                    htmlString = act.text,
                                    linkStyles = TextLinkStyles(SpanStyle(Color(settings.linkColor)))
                                ),
                                style = LocalTextStyle.current.copy(
                                    color = Color(settings.textColor),
                                    fontSize = settings.fontSize.sp,
                                    fontFamily = FontFamily(settings.typeface.toTypeface()),
                                    background = Color(settings.backColor)
                                )
                            )
                        }
                    )
                    HorizontalDivider()
                }
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
        LibGenItem(text = "Action 5", imagePath = "")
    )
    MainContentActionsList(
        countActsVis = 3,
        settings = settings,
        actions = actions,
        onActionClicked = {},
        gridState = rememberLazyStaggeredGridState()
    )
}
