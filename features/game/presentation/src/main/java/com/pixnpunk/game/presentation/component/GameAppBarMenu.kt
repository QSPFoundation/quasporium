package com.pixnpunk.game.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pixnpunk.game.presentation.R

@Composable
fun RootAppBarMenu(
    doCreateSaveIntent: () -> Unit,
    doCreateLoadIntent: () -> Unit
) {
    Row {
        Box {
            IconButton(onClick = doCreateSaveIntent) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.act_save)
                )
            }
        }
        Box {
            IconButton(onClick = doCreateLoadIntent) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = stringResource(R.string.act_load)
                )
            }
        }
    }
}