package com.pixnpunk.details.presentation.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.R

@Composable
fun DetailsDescription(
    descriptionHtml: String
) {
    Card(
        modifier = Modifier.padding(4.dp)
    ) {
        var isExpanded by remember { mutableStateOf(false) }
        var isExpandable by remember { mutableStateOf(false) }

        val text = remember(descriptionHtml) {
            AnnotatedString.fromHtml(descriptionHtml)
        }

        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.description_title),
                    style = MaterialTheme.typography.titleMedium
                )

                AnimatedVisibility(
                    visible = isExpandable
                ) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded }
                    ) {
                        Icon(
                            imageVector =
                                if (isExpanded) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                            contentDescription =
                                if (isExpanded) stringResource(R.string.collapse_button_description)
                                else stringResource(R.string.expand_button_description)
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = {
                    val duration = 600
                    if (targetState) {
                        fadeIn(animationSpec = snap()) togetherWith
                        fadeOut(animationSpec = snap()) using
                        SizeTransform { _, _ -> tween(duration) }
                    } else {
                        fadeIn(animationSpec = snap(delayMillis = duration)) togetherWith
                        fadeOut(animationSpec = snap(delayMillis = duration)) using
                        SizeTransform { _, _ -> tween(duration) }
                    }
                },
                contentAlignment = Alignment.TopStart,
                label = "text_expansion_animation"
            ) { targetExpanded ->
                Text(
                    modifier = Modifier.clearAndSetSemantics { contentDescription = text.text },
                    text = text,
                    maxLines = if (targetExpanded) Int.MAX_VALUE else 3,
                    overflow = if (targetExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult ->
                        if (!targetExpanded) isExpandable = textLayoutResult.hasVisualOverflow
                    }
                )
            }
        }
    }
}