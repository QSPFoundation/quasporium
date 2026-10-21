package com.pixnpunk.details.presentation.content.preset.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixnpunk.details.presentation.content.preset.EditColorItem
import com.pixnpunk.details.presentation.content.preset.EditImageItem
import com.pixnpunk.details.presentation.theme.LocalProfileColors
import com.pixnpunk.details.presentation.theme.ProfileThemePreset

@Composable
fun EditRootColumn(
    selectedColor: Long,
    backImagePath: String,
    editedName: String,
    onPickBackground: () -> Unit,
    onRemoveBackground: () -> Unit,
    onSelectColor: (newColor: Long) -> Unit,
    onSaveVisuals: (newName: String, newColor: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ACCENT COLOR",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = LocalProfileColors.current.onCardContainer.copy(alpha = 0.6f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileThemePreset.entries.forEach { preset ->
                val isSelected = selectedColor == preset.seedColor
                EditColorItem(
                    color = Color(preset.seedColor),
                    isSelected = isSelected,
                    onClick = { onSelectColor(preset.seedColor) }
                )
            }
        }

        Text(
            text = "BACKGROUND IMAGE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = LocalProfileColors.current.onCardContainer.copy(alpha = 0.6f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditImageItem(
                backImagePath = backImagePath,
                onPickBackground = onPickBackground,
                onRemoveBackground = onRemoveBackground
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    onSaveVisuals(
                        editedName,
                        selectedColor
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalProfileColors.current.primaryButton,
                    contentColor = LocalProfileColors.current.onPrimaryButton
                )
            ) { Text("Save") }
        }
    }
}
