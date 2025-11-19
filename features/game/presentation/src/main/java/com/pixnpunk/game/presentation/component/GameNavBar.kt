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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.pixnpunk.game.presentation.GameComponent
import com.pixnpunk.game.presentation.R
import com.pixnpunk.game.presentation.RealGameComponent

@Composable
fun GameNavBar(
    component: GameComponent,
    activeComponent: GameComponent.Child
) {
    val state by component.model.collectAsState()

    BottomAppBar {
        NavigationBarItem(
            enabled = state.isExtraEnabled,
            icon = {
                BadgedBox(
                    badge = { }
                ) {
                    if (activeComponent is GameComponent.Child.ExtraChild) {
                        Icon(Icons.Filled.Sell, contentDescription = "")
                    } else {
                        Icon(Icons.Outlined.Sell, contentDescription = "")
                    }

                }
            },
            label = { Text(stringResource(R.string.tab_extra)) },
            alwaysShowLabel = false,
            onClick = { component.navigation(RealGameComponent.ChildConfig.Extra) },
            selected = activeComponent is GameComponent.Child.ExtraChild,
        )
        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = { }
                ) {
                    Icon(Icons.Filled.Menu, contentDescription = "")
                }
            },
            label = { Text(stringResource(R.string.tab_main)) },
            alwaysShowLabel = false,
            onClick = { component.navigation(RealGameComponent.ChildConfig.Main) },
            selected = activeComponent is GameComponent.Child.MainChild,
        )
        NavigationBarItem(
            enabled = state.isObjectsEnabled,
            icon = {
                BadgedBox(
                    badge = { }
                ) {
                    if (activeComponent is GameComponent.Child.ObjectChild) {
                        Icon(Icons.Filled.Work, contentDescription = "")
                    } else {
                        Icon(Icons.Outlined.WorkOutline, contentDescription = "")
                    }
                }
            },
            label = { Text(stringResource(R.string.tab_object)) },
            alwaysShowLabel = false,
            onClick = { component.navigation(RealGameComponent.ChildConfig.Object) },
            selected = activeComponent is GameComponent.Child.ObjectChild,
        )
    }
}