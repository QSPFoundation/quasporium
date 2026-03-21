package com.pixnpunk.details.presentation.content

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pixnpunk.details.presentation.DetailsComponent
import com.pixnpunk.details.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsAppBar(
    component: DetailsComponent
) {
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(
                onClick = component.onDismissed
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.back_button_description)
                )
            }
        }
    )
}