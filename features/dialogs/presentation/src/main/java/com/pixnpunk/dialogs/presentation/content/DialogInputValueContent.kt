package com.pixnpunk.dialogs.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun DialogInputValueContent(
    initialValue: String,
    label: @Composable () -> Unit,
    inputValue: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    onDismiss: () -> Unit
) {
    var enterValue by remember { mutableStateOf(initialValue) }

    val isEnable = when (keyboardOptions.keyboardType) {
        KeyboardType.Number -> enterValue.toIntOrNull() != null
        else -> false
    }

    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = enterValue,
            onValueChange = { enterValue = it },
            label = label,
            singleLine = true,
            keyboardOptions = keyboardOptions
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
            TextButton(
                onClick = {
                    inputValue(enterValue)
                    onDismiss()
                },
                enabled = isEnable
            ) {
                Text("Enter")
            }
        }
    }
}