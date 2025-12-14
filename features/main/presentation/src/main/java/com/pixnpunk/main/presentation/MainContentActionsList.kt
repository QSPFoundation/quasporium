package com.pixnpunk.main.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.utils.ViewUtil.toTypeface

@Composable
fun MainContentActionsList(
    countActsVis: Int,
    settings: SettingsEntity,
    actions: List<LibGenItem>,
    onActionClicked: (Int) -> Unit,
    listState: LazyListState
) {
    var listItemHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                if (actions.size > countActsVis) {
                    listItemHeight * countActsVis
                } else {
                    listItemHeight * actions.size
                }
            ),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(actions) { key, act ->
            Column(
                modifier = Modifier
                    .padding(start = 8.dp, end = 8.dp)
                    .then(
                        if (key == 0) Modifier.onGloballyPositioned { crd ->
                            listItemHeight = with(density) { crd.size.height.toDp() }
                        } else Modifier
                    )
            ) {
                ListItem(
                    modifier = Modifier.clickable(true) { onActionClicked(key) },
                    colors = ListItemDefaults.colors(
                        containerColor = Color(settings.backColor)
                    ),
                    leadingContent = {
                        if (act.imagePath.isNotBlank()) {
                            AsyncImage(
                                model = act.imagePath,
                                contentDescription = ""
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