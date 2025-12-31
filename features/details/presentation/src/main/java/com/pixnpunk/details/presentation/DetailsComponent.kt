package com.pixnpunk.details.presentation

import com.pixnpunk.dto.GameEntry

interface DetailsComponent {
    val showSelectGameDialog: (GameEntry) -> Unit
    val showEditGameDialog: (GameEntry) -> Unit
    val showDownloadGameDialog: (GameEntry) -> Unit
    val onDismissed: () -> Unit
}