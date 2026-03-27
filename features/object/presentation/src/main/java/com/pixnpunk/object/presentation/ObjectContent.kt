package com.pixnpunk.`object`.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.pixnpunk.utils.ViewUtil.toTypeface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectContent(
    component: ObjectComponent,
    dismiss: () -> Unit
) {
    val state by component.model.collectAsState()
    val settings by component.settingsState.collectAsState()

    val customFontFamily = remember(settings.typeface) {
        FontFamily(settings.typeface.toTypeface())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Objects") },
                navigationIcon = {
                    IconButton(onClick = dismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        containerColor = Color(settings.backColor)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            itemsIndexed(state.objects) { key, obj ->
                val annotatedText = remember(obj.text) {
                    AnnotatedString.fromHtml(
                        htmlString = obj.text,
                        linkStyles = TextLinkStyles(SpanStyle(Color(settings.linkColor)))
                    )
                }

                ListItem(
                    modifier = Modifier.clickable { component.onObjectSelected(key) },
                    colors = ListItemDefaults.colors(
                        containerColor = Color(settings.backColor)
                    ),
                    leadingContent = {
                        if (obj.imagePath.isNotBlank()) {
                            AsyncImage(
                                model = obj.imagePath,
                                contentDescription = null
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            modifier = Modifier.clearAndSetSemantics {
                                contentDescription = annotatedText.text
                            },
                            text = annotatedText,
                            style = LocalTextStyle.current.copy(
                                color = Color(settings.textColor),
                                fontSize = settings.fontSize.sp,
                                fontFamily = customFontFamily,
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