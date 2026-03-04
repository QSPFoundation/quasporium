package com.pixnpunk.dialogs.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Scale
import com.pixnpunk.dialogs.presentation.R

@Composable
fun DialogOutputValueContent(
    message: String,
    isImage: Boolean,
    isMsg: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isImage) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(message.toUri())
                    .scale(Scale.FILL)
                    .build(),
                contentDescription = null
            )
        } else {
            Text(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                text = remember(message) {
                    AnnotatedString.fromHtml(
                        message
                            .replace("\r\n", "<br>")
                            .replace("\n", "<br>")
                            .replace("\r", "<br>")
                    )
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (!isMsg) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(stringResource(R.string.cancel_button_description))
                }
            }
            TextButton(
                onClick = { onConfirm(); onDismiss() }
            ) {
                Text(stringResource(R.string.confirm_button_description))
            }
        }
    }
}