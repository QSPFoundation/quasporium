package com.pixnpunk.details.presentation.content.preset

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditImageItem(
    backImagePath: String,
    onPickBackground: () -> Unit,
    onRemoveBackground: () -> Unit,
) {
    if (backImagePath.isBlank()) {
        OutlinedButton(
            onClick = onPickBackground,
            shape = CircleShape,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Add background", style = MaterialTheme.typography.labelMedium)
        }
    } else {
        InputChip(
            selected = true,
            onClick = onPickBackground,
            label = { Text("Custom image") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(
                    modifier = Modifier.size(20.dp),
                    onClick = onRemoveBackground
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove background",
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            shape = CircleShape
        )
    }
}