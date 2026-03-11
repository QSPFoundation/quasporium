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
    doCloseGame: () -> Unit,
    doSaveFile: () -> Unit,
    doLoadFile: () -> Unit
) {
    val state by component.model.collectAsState()

    val isInputExecutorVis = state.isInputEnabled || state.isExecutorEnabled

    TopAppBar(
        title = { Text(titleText) },
        navigationIcon = {
            IconButton(onClick = doCloseGame) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.exit_button_description)
                )
            }
        },
        actions = {
            IconButton(onClick = { component.doRestartGame() }) {
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
            if (state.isGameRunning && !state.isGameDirInternal) {
                RootAppBarMenu(
                    doCreateSaveIntent = doSaveFile,
                    doCreateLoadIntent = doLoadFile
                )
            }
        }
    )
}