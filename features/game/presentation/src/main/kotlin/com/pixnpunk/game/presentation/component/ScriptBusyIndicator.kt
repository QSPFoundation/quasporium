package com.pixnpunk.game.presentation.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.pixnpunk.game.presentation.R

@Composable
internal fun BoxScope.ScriptBusyIndicator(isBusy: Boolean) {
    if (isBusy) {
        val desc = stringResource(R.string.script_running_description)
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .semantics { contentDescription = desc }
        )
    }
}
