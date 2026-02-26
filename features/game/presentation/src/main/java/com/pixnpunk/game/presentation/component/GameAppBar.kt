package com.pixnpunk.game.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.pixnpunk.game.presentation.GameComponent
import com.pixnpunk.game.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameAppBar(
    titleText: String,
    component: GameComponent,
    showDialog: () -> Unit,
    onFinishActivity: () -> Unit,
    doCreateSaveIntent: () -> Unit,
    doCreateLoadIntent: () -> Unit
) {
    val state by component.model.collectAsState()

    val isInputExecutorVis = state.isInputEnabled || state.isExecutorEnabled

    TopAppBar(
        title = { Text(titleText) },
        navigationIcon = {
            IconButton(onClick = onFinishActivity) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.exit_button_description)
                )
            }
        },
        actions = {
            IconButton(onClick = { component.restartGame() }) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = stringResource(R.string.restart_button_description)
                )
            }
            if (isInputExecutorVis) {
                IconButton(
                    onClick = showDialog
                ) {
                    Icon(
                        imageVector = Icons.Filled.Terminal,
                        contentDescription = null
                    )
                }
            }
            if (state.isGameRunning) {
                RootAppBarMenu(
                    doCreateSaveIntent = doCreateSaveIntent,
                    doCreateLoadIntent = doCreateLoadIntent
                )
            }
        }
    )
}