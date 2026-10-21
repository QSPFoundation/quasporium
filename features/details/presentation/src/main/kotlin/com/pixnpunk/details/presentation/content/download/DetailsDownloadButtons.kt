package com.pixnpunk.details.presentation.content.download

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.R

@Composable
fun DetailsDownloadButtons(
    onDownload: () -> Unit
) {
    FilledTonalButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onDownload
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            style = MaterialTheme.typography.titleMedium,
            text = stringResource(R.string.download_button_description)
        )
    }
    // TODO ADD "DOWNLOAD AND ADD PROFILE"
}
