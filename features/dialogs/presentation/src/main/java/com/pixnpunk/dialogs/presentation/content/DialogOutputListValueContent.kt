package com.pixnpunk.dialogs.presentation.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.utils.ViewUtil.toTypeface

@Composable
fun DialogOutputListValueContent(
    listItems: List<LibGenItem>,
    backColor: Color,
    textColor: Color,
    linkColor: Color,
    fontSize: TextUnit,
    fontFamily: FontFamily,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn {
            itemsIndexed(listItems) { key, value ->
                ListItem(
                    modifier = Modifier.clickable(true) {
                        onConfirm(key)
                        onDismiss()
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = backColor
                    ),
                    headlineContent = {
                        Text(
                            text = AnnotatedString.fromHtml(
                                htmlString = value.text,
                                linkStyles = TextLinkStyles(SpanStyle(linkColor))
                            ),
                            style = LocalTextStyle.current.copy(
                                color = textColor,
                                fontSize = fontSize,
                                fontFamily = fontFamily,
                                background = backColor
                            )
                        )
                    }
                )
            }
        }
    }
}