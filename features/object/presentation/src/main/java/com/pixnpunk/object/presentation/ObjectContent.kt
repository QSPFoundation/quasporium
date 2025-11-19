package com.pixnpunk.`object`.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.sp
import com.pixnpunk.utils.ViewUtil.toTypeface

@Composable
fun ObjectContent(
    component: ObjectComponent
) {
    val state by component.model.collectAsState()
    val settings by component.settingsFlow.collectAsState()

    Surface(
        color = Color(settings.backColor)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(state.objects) { key, obj ->
                ListItem(
                    modifier = Modifier.clickable { component.onObjectSelected(key) },
                    colors = ListItemDefaults.colors(
                        containerColor = Color(settings.backColor)
                    ),
                    headlineContent = {
                        Text(
                            text = AnnotatedString.fromHtml(
                                htmlString = obj.text,
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