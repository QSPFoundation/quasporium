package com.pixnpunk.dialogs.presentation.content.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixnpunk.dialogs.presentation.R

@Composable
internal fun AddGameActionButtons(
    buttonText: String,
    isConfirmEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = onDismiss
        ) {
            Text(stringResource(R.string.cancel_button_description))
        }

        Spacer(Modifier.width(8.dp))

        Button(
            onClick = onConfirm,
            enabled = isConfirmEnabled
        ) {
            Text(buttonText)
        }
    }
}
