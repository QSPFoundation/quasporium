package com.pixnpunk.stock.presentation.content

import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.Composable

@Composable
fun StockFloatButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    FloatingActionButton(onClick = onClick, content = icon)
}