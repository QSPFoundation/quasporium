package com.pixnpunk.dialogs.presentation.content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dialogs.presentation.R
import com.pixnpunk.utils.FileUtil.receiveFullName

@Composable
fun DialogSelectGameContent(
    component: DialogsComponent
) {
    val entry = component.dialogsConfig.unfilledEntry

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(bottom = 16.dp),
            text = stringResource(R.string.file_run_title),
            style = MaterialTheme.typography.headlineSmall
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(entry.gameFilesPath) { index, item ->
                ListItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            component.onDismissed
                        },
                    headlineContent = { Text(text = item.receiveFullName()) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = component.onDismissed) {
                Text(stringResource(R.string.cancel_button_description))
            }
        }
    }
}