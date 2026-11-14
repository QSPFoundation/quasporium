package com.pixnpunk.details.presentation.content.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.R
import com.pixnpunk.stock.domain.model.InstallState

private const val PERCENT = 100

@Composable
private fun DownloadButton(text: String, onClick: () -> Unit) {
    FilledTonalButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            style = MaterialTheme.typography.titleMedium,
            text = text
        )
    }
}

@Composable
private fun InstallProgress(label: String, progress: Float?, onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            if (progress == null) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            else LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
        OutlinedButton(onClick = onCancel) {
            Text(text = stringResource(R.string.details_install_cancel))
        }
    }
}

@Composable
fun DetailsDownloadButtons(
    installState: InstallState,
    onDownload: () -> Unit,
    onCancel: () -> Unit
) {
    when (installState) {
        InstallState.NotStarted ->
            DownloadButton(stringResource(R.string.download_button_description), onDownload)

        InstallState.Failed ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.details_install_failed),
                    color = MaterialTheme.colorScheme.error
                )
                DownloadButton(stringResource(R.string.details_install_retry), onDownload)
            }

        InstallState.Installed ->
            Text(
                text = stringResource(R.string.details_install_installed),
                style = MaterialTheme.typography.bodyMedium
            )

        InstallState.Queued ->
            InstallProgress(stringResource(R.string.details_install_waiting), null, onCancel)

        InstallState.Paused ->
            InstallProgress(stringResource(R.string.details_install_paused), null, onCancel)

        is InstallState.Downloading ->
            InstallProgress(
                stringResource(R.string.details_install_downloading, (installState.progress * PERCENT).toInt()),
                installState.progress,
                onCancel
            )

        is InstallState.Unpacking ->
            InstallProgress(
                stringResource(R.string.details_install_unpacking, (installState.progress * PERCENT).toInt()),
                installState.progress,
                onCancel
            )
    }
    // TODO ADD "DOWNLOAD AND ADD PROFILE"
}
