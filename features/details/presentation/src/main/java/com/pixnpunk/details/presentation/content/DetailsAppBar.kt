package com.pixnpunk.details.presentation.content

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.pixnpunk.details.presentation.DetailsComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsAppBar(
    component: DetailsComponent
) {
    TopAppBar(
        title = { Text("") },
        navigationIcon = {
            IconButton(
                onClick = component.onDismissed
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = ""
                )
            }
        }
    )
}