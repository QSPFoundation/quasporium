package com.pixnpunk.game.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pixnpunk.game.presentation.GameComponent
import com.pixnpunk.game.presentation.R
import com.pixnpunk.game.presentation.RealGameComponent

@Composable
fun GameBottomBarExpand(
    isExtraEnabled: Boolean,
    isObjectsEnabled: Boolean,
    activeComponent: GameComponent.ExpandChild,
    navigationTo: (RealGameComponent.ExpandChildConfig) -> Unit,
    content: @Composable (() -> Unit)
) {
    NavigationSuiteScaffold(
        content = content,
        navigationSuiteItems = {
            item(
                enabled = isExtraEnabled,
                icon = {
                    BadgedBox(
                        badge = { }
                    ) {
                        if (activeComponent is GameComponent.ExpandChild.ExtraChild) {
                            Icon(Icons.Filled.Sell, contentDescription = null)
                        } else {
                            Icon(Icons.Outlined.Sell, contentDescription = null)
                        }
                    }
                },
                label = { Text(stringResource(R.string.extra_tab_title)) },
                alwaysShowLabel = false,
                onClick = { navigationTo(RealGameComponent.ExpandChildConfig.Extra) },
                selected = activeComponent is GameComponent.ExpandChild.ExtraChild
            )
            item(
                icon = {
                    BadgedBox(
                        badge = { }
                    ) {
                        Icon(Icons.Filled.Menu, contentDescription = null)
                    }
                },
                label = { Text(stringResource(R.string.main_tab_title)) },
                alwaysShowLabel = false,
                onClick = { navigationTo(RealGameComponent.ExpandChildConfig.MainActions) },
                selected = activeComponent is GameComponent.ExpandChild.MainActionsChild
            )
            item(
                enabled = isObjectsEnabled,
                icon = {
                    BadgedBox(
                        badge = { }
                    ) {
                        if (activeComponent is GameComponent.ExpandChild.ObjectChild) {
                            Icon(Icons.Filled.Work, contentDescription = null)
                        } else {
                            Icon(Icons.Outlined.WorkOutline, contentDescription = null)
                        }
                    }
                },
                label = { Text(stringResource(R.string.object_tab_title)) },
                alwaysShowLabel = false,
                onClick = { navigationTo(RealGameComponent.ExpandChildConfig.Object) },
                selected = activeComponent is GameComponent.ExpandChild.ObjectChild
            )
        }
    )
}