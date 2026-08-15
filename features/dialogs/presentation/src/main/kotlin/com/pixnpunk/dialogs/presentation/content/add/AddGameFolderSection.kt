package com.pixnpunk.dialogs.presentation.content.add

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pixnpunk.dialogs.presentation.R
import com.pixnpunk.utils.FileUtil.receiveDirRelPath

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun AddGameFolderSection(
    isDirSelected: Boolean,
    gameDirPath: String,
    onSelectDir: () -> Unit
) {
    FilledTonalButton(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onSelectDir
    ) {
        Icon(
            modifier = Modifier.padding(end = 8.dp),
            imageVector = Icons.Default.CreateNewFolder,
            contentDescription = null
        )
        Text(
            if (isDirSelected) gameDirPath.receiveDirRelPath()
            else stringResource(R.string.folder_selector_title),
            style = MaterialTheme.typography.titleMedium
        )
    }
}