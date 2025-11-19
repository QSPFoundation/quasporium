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
    component: GameComponent,
    showDialog: () -> Unit,
    activeComponent: GameComponent.Child,
    onFinishActivity: () -> Unit
) {
    val state by component.model.collectAsState()

    val isInputExecutorVis = state.isInputEnabled || state.isExecutorEnabled

    TopAppBar(
        title = {
            Text(
                when (activeComponent) {
                    is GameComponent.Child.MainChild -> stringResource(R.string.tab_main)
                    is GameComponent.Child.ExtraChild -> stringResource(R.string.tab_extra)
                    is GameComponent.Child.ObjectChild -> stringResource(R.string.tab_object)
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onFinishActivity) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.act_exit)
                )
            }
        },
        actions = {
            IconButton(onClick = { component.restartGame() }) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = stringResource(R.string.act_restart)
                )
            }
            if (isInputExecutorVis) {
                IconButton(
                    onClick = showDialog
                ) {
                    Icon(
                        imageVector = Icons.Filled.Terminal,
                        contentDescription = ""
                    )
                }
            }
            if (state.isGameRunning) {
                RootAppBarMenu(component)
            }
        }
    )
}