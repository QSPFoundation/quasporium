package com.pixnpunk.game.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pixnpunk.game.presentation.R

@Composable
fun GameBottomBar(
    isExtraEnabled: Boolean,
    isObjectsEnabled: Boolean,
    extraSlotIsNotEmpty: Boolean,
    activateExtraSlot: () -> Unit,
    objectSlotIsNotEmpty: Boolean,
    activateObjectSlot: () -> Unit,
    cleanAllSlot: () -> Unit,
    content: @Composable (() -> Unit)
) {
    NavigationSuiteScaffold(
        content = content,
        navigationItemVerticalArrangement = Arrangement.Center,
        navigationItems = {
            NavigationSuiteItem(
                enabled = isExtraEnabled,
                icon = {
                    if (extraSlotIsNotEmpty) {
                        Icon(Icons.Filled.Sell, contentDescription = null)
                    } else {
                        Icon(Icons.Outlined.Sell, contentDescription = null)
                    }
                },
                label = { Text(stringResource(R.string.extra_tab_title)) },
                onClick = activateExtraSlot,
                selected = extraSlotIsNotEmpty,
            )
            NavigationSuiteItem(
                icon = {
                    Icon(Icons.Filled.Menu, contentDescription = null)
                },
                label = { Text(stringResource(R.string.main_tab_title)) },
                onClick = cleanAllSlot,
                selected = !extraSlotIsNotEmpty && !objectSlotIsNotEmpty
            )
            NavigationSuiteItem(
                enabled = isObjectsEnabled,
                icon = {
                    if (objectSlotIsNotEmpty) {
                        Icon(Icons.Filled.Work, contentDescription = null)
                    } else {
                        Icon(Icons.Outlined.WorkOutline, contentDescription = null)
                    }
                },
                label = { Text(stringResource(R.string.object_tab_title)) },
                onClick = activateObjectSlot,
                selected = objectSlotIsNotEmpty
            )
        }
    )
}