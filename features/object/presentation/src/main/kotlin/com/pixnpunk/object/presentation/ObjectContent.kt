package com.pixnpunk.`object`.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pixnpunk.`object`.presentation.content.ObjectCell

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectContent(
    component: ObjectComponent,
    dismiss: () -> Unit
) {
    val uiState by component.uiState.collectAsStateWithLifecycle()

    val textStyle = MaterialTheme.typography.bodyLarge.merge(
        TextStyle(
            color = Color(uiState.textColor),
            fontSize = uiState.fontSize.sp,
            fontFamily = uiState.fontFamily,
            background = Color(uiState.backColor)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Objects") },
                navigationIcon = {
                    IconButton(onClick = dismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        containerColor = Color(uiState.backColor)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            itemsIndexed(
                items = uiState.parsedObjects
            ) { key, obj ->
                ObjectCell(
                    item = obj,
                    textStyle = textStyle,
                    backColor = Color(uiState.backColor),
                    onItemClick = { component.onObjectSelected(key) }
                )
                HorizontalDivider()
            }
        }
    }
}