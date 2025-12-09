package com.pixnpunk.dialogs.presentation

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import com.pixnpunk.dialogs.presentation.content.DialogAddGameContent
import com.pixnpunk.dialogs.presentation.content.DialogEditGameContent
import com.pixnpunk.dialogs.presentation.content.DialogInputValueContent
import com.pixnpunk.dialogs.presentation.content.DialogOutputListValueContent
import com.pixnpunk.dialogs.presentation.content.DialogOutputValueContent
import com.pixnpunk.dialogs.presentation.content.DialogSelectColorContent
import com.pixnpunk.dialogs.presentation.content.DialogSelectGameContent
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.OutputState
import com.pixnpunk.settings.SettingsRepository
import com.pixnpunk.utils.ViewUtil.toTypeface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogsMainContent(
    component: DialogsComponent,
    settingsRepository: SettingsRepository
) {
    val config = component.dialogsConfig
    val settings by settingsRepository.settingsFlow.collectAsState()

    BasicAlertDialog(
        onDismissRequest = component.onDismissed
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            when (config.dialogsState) {
                DialogsState.DIALOG_SELECT_RUN_GAME ->
                    DialogSelectGameContent(component)

                DialogsState.DIALOG_ADD_GAME ->
                    DialogAddGameContent(
                        entry = config.unfilledEntry,
                        selectFile = component.selectFile,
                        deselectFile = component.deselectFile,
                        selectFolder = component.selectFolder,
                        addGame = {
                            component.addNewEntry(it)
                            component.onDismissed()
                        },
                        buttonText = "Add game"
                    )

                DialogsState.DIALOG_DOWNLOAD_GAME ->
                    DialogAddGameContent(
                        entry = config.unfilledEntry,
                        selectFile = component.selectFile,
                        deselectFile = component.deselectFile,
                        selectFolder = component.selectFolder,
                        addGame = {
                            component.downloadNewEntry(it)
                            component.onDismissed()
                        },
                        buttonText = "Download game"
                    )

                DialogsState.DIALOG_EDIT_GAME ->
                    DialogEditGameContent(
                        entry = config.unfilledEntry,
                        selectFile = component.selectFile,
                        editGame = {
                            component.editEntry(it)
                            component.onDismissed()
                        }
                    )

                DialogsState.DIALOG_SELECT_COLOR -> config.colorPickerState.let {
                    if (it == null) {
                        component.onDismissed()
                    } else {
                        DialogSelectColorContent(
                            selectColor = { c -> component.selectColor(it to c) },
                            initialColor = Color(
                                when (it) {
                                    ColorPickerState.COLOR_TEXT -> settings.textColor
                                    ColorPickerState.COLOR_BACK -> settings.backColor
                                    ColorPickerState.COLOR_LINK -> settings.linkColor
                                }
                            ),
                            onDismiss = component.onDismissed
                        )
                    }
                }

                DialogsState.DIALOG_INPUT_VALUE -> config.inputState.let {
                    if (it == null) {
                        component.onDismissed()
                    } else {
                        DialogInputValueContent(
                            initialValue = "${
                                when (it) {
                                    InputState.INPUT_IMAGE_WIDTH -> settings.imageWidth
                                    InputState.INPUT_IMAGE_HEIGHT -> settings.imageHeight
                                    InputState.INPUT_WITH_MESSAGE -> ""
                                }
                            }",
                            label = {
                                Text(
                                    when (it) {
                                        InputState.INPUT_IMAGE_WIDTH -> "Image width"
                                        InputState.INPUT_IMAGE_HEIGHT -> "Image height"
                                        InputState.INPUT_WITH_MESSAGE -> config.outputValue
                                    }
                                )
                            },
                            inputValue = { s -> component.inputValue(it to s) },
                            keyboardOptions =
                                when (it) {
                                    InputState.INPUT_IMAGE_WIDTH, InputState.INPUT_IMAGE_HEIGHT ->
                                        KeyboardOptions(keyboardType = KeyboardType.Number)

                                    InputState.INPUT_WITH_MESSAGE ->
                                        KeyboardOptions()
                                },
                            onDismiss = component.onDismissed
                        )
                    }
                }

                DialogsState.DIALOG_OUTPUT_VALUE -> config.outputState.let {
                    if (it == null || !config.outputValue.isBlank()) {
                        component.onDismissed()
                    } else {
                        if (it != OutputState.OUTPUT_LIST) {
                            DialogOutputValueContent(
                                message = config.outputValue,
                                isImage = it == OutputState.OUTPUT_IMAGE,
                                onConfirm = { component.confirmMessage(it) },
                                onDismiss = component.onDismissed
                            )
                        } else {
                            DialogOutputListValueContent(
                                listItems = config.menuItems,
                                backColor = Color(settings.backColor),
                                textColor = Color(settings.textColor),
                                linkColor = Color(settings.linkColor),
                                fontSize = settings.fontSize.sp,
                                fontFamily = FontFamily(settings.typeface.toTypeface()),
                                onConfirm = { _ -> component.confirmMessage(it) },
                                onDismiss = component.onDismissed
                            )
                        }
                    }
                }
            }
        }
    }
}