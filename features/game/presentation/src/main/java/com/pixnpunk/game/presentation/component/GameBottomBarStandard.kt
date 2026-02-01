package com.pixnpunk.game.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pixnpunk.game.presentation.GameComponent
import com.pixnpunk.game.presentation.R
import com.pixnpunk.game.presentation.RealGameComponent

@Composable
fun GameBottomBarStandard(
    isExtraEnabled: Boolean,
    isObjectsEnabled: Boolean,
    activeComponent: GameComponent.StandardChild,
    navigationTo: (RealGameComponent.StandardChildConfig) -> Unit
) {
    BottomAppBar {
        NavigationBarItem(
            enabled = isExtraEnabled,
            icon = {
                BadgedBox(
                    badge = { }
                ) {
                    if (activeComponent is GameComponent.StandardChild.ExtraChild) {
                        Icon(Icons.Filled.Sell, contentDescription = null)
                    } else {
                        Icon(Icons.Outlined.Sell, contentDescription = null)
                    }
                }
            },
            label = { Text(stringResource(R.string.extra_tab_title)) },
            alwaysShowLabel = false,
            onClick = { navigationTo(RealGameComponent.StandardChildConfig.Extra) },
            selected = activeComponent is GameComponent.StandardChild.ExtraChild,
        )
        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = { }
                ) {
                    Icon(Icons.Filled.Menu, contentDescription = null)
                }
            },
            label = { Text(stringResource(R.string.main_tab_title)) },
            alwaysShowLabel = false,
            onClick = { navigationTo(RealGameComponent.StandardChildConfig.Main) },
            selected = activeComponent is GameComponent.StandardChild.MainChild,
        )
        NavigationBarItem(
            enabled = isObjectsEnabled,
            icon = {
                BadgedBox(
                    badge = { }
                ) {
                    if (activeComponent is GameComponent.StandardChild.ObjectChild) {
                        Icon(Icons.Filled.Work, contentDescription = null)
                    } else {
                        Icon(Icons.Outlined.WorkOutline, contentDescription = null)
                    }
                }
            },
            label = { Text(stringResource(R.string.object_tab_title)) },
            alwaysShowLabel = false,
            onClick = { navigationTo(RealGameComponent.StandardChildConfig.Object) },
            selected = activeComponent is GameComponent.StandardChild.ObjectChild,
        )
    }
}