package com.pixnpunk.details.presentation.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pixnpunk.details.presentation.content.download.DetailsDownloadButtons
import com.pixnpunk.details.presentation.content.preset.DetailsPresetsCarouselDock
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.details.presentation.model.PresetUIState
import com.pixnpunk.dto.GameListType
import com.pixnpunk.stock.domain.model.InstallState

@Composable
fun DetailsButtonsSet(
    isLoading: Boolean,
    gameType: GameListType,
    presets: List<PresetUIState>,
    files: List<GameFileItem>,
    onCreateProfile: () -> Unit,
    onUpdateVisuals: (presetId: Long, newName: String, newColor: Long) -> Unit,
    onPickBackground: (presetId: Long) -> Unit,
    onRemoveBackground: (presetId: Long) -> Unit,
    onDeleteProfile: (presetId: Long) -> Unit,
    installState: InstallState,
    onDownload: () -> Unit,
    onCancelInstall: () -> Unit,
    onContinue: (presetId: Long, savePath: String) -> Unit,
    onStartFresh: (presetId: Long, fileName: String) -> Unit
) {
    Column(
        modifier = Modifier.padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (gameType) {
            GameListType.Unknown -> {}

            GameListType.Downloadable ->
                DetailsDownloadButtons(
                    installState = installState,
                    onDownload = onDownload,
                    onCancel = onCancelInstall
                )

            GameListType.Playable ->
                DetailsPresetsCarouselDock(
                    isLoading = isLoading,
                    presets = presets,
                    files = files,
                    onCreateProfile = onCreateProfile,
                    onUpdateVisuals = onUpdateVisuals,
                    onPickBackground = onPickBackground,
                    onRemoveBackground = onRemoveBackground,
                    onDeleteProfile = onDeleteProfile,
                    onContinue = onContinue,
                    onStartFresh = onStartFresh
                )
        }
    }
}
