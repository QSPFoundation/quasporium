package com.pixnpunk.dialogs.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.AlphaTile
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.pixnpunk.dialogs.presentation.R

@Composable
fun DialogSelectColorContent(
    selectColor: (Color) -> Unit,
    initialColor: Color,
    onDismiss: () -> Unit
) {
    val controller = rememberColorPickerController()

    var selectedColor by remember { mutableStateOf(initialColor) }
    var selectedHex by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(8f)) {
            HsvColorPicker(
                modifier = Modifier.padding(10.dp),
                controller = controller,
                onColorChanged = { colorEnvelope ->
                    selectedHex = colorEnvelope.hexCode
                    selectedColor = colorEnvelope.color
                },
                initialColor = initialColor,
            )
        }

        AlphaSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(35.dp)
                .align(Alignment.CenterHorizontally),
            controller = controller,
            initialColor = initialColor
        )

        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .height(35.dp)
                .align(Alignment.CenterHorizontally),
            controller = controller,
            initialColor = initialColor
        )

        Text(
            text = "#$selectedHex",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        AlphaTile(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(6.dp))
                .align(Alignment.CenterHorizontally),
            controller = controller,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.cancel_button_description))
            }
            TextButton(
                onClick = {
                    selectColor(selectedColor)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.select_button_description))
            }
        }
    }
}