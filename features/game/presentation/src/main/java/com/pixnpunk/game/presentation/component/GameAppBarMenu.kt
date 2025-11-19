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
import com.pixnpunk.game.presentation.GameComponent
import com.pixnpunk.game.presentation.R

@Composable
fun RootAppBarMenu(
    component: GameComponent
) {
    Row {
        Box {
            IconButton(onClick = component::doCreateSaveIntent) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.act_save)
                )
            }
        }
        Box {
            IconButton(onClick = component::doCreateLoadIntent) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = stringResource(R.string.act_load)
                )
            }
        }
    }
}