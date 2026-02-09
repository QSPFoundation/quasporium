package com.pixnpunk.dialogs.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.utils.ViewUtil.toTypeface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogsMainContent(
    component: DialogsComponent,
    settings: SettingsEntity
) {
    val config = component.dialogsConfig
    val state = config.dialogsState

    if (state == DialogsState.DIALOG_ADD_GAME
        || state == DialogsState.DIALOG_EDIT_GAME
    ) {
        BasicAlertDialog(
            onDismissRequest = component.onDismissed,
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                when (state) {
                    DialogsState.DIALOG_ADD_GAME ->
                        DialogAddGameContent(
                            entry = component.dialogsConfig.unfilledEntry,
                            selectFile = component.selectFile,
                            deselectFile = { component.deselectFile(it) },
                            selectDir = component.selectFolder,
                            deselectDir = component.deselectDir,
                            selectFiles = component.selectFiles,
                            addGame = {
                                component.addNewEntry(it)
                                component.onDismissed()
                            },
                            onDismiss = component.onDismissed,
                            isAddLocalGame = true,
                            buttonText = stringResource(R.string.add_button_description)
                        )

                    DialogsState.DIALOG_EDIT_GAME ->
                        DialogEditGameContent(
                            entry = component.dialogsConfig.unfilledEntry,
                            selectFile = component.selectFile,
                            deselectFile = { component.deselectFile(it) },
                            selectFiles = component.selectFiles,
                            editGame = {
                                component.editEntry(it)
                                component.onDismissed()
                            }
                        )

                    else -> {}
                }
            }
        }
    } else {
        BasicAlertDialog(
            onDismissRequest = component.onDismissed,
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                when (state) {
                    DialogsState.DIALOG_SELECT_RUN_GAME -> DialogSelectGameContent(component)

                    DialogsState.DIALOG_DOWNLOAD_GAME ->
                        DialogAddGameContent(
                            entry = component.dialogsConfig.unfilledEntry,
                            selectFile = component.selectFile,
                            deselectFile = component.deselectFile,
                            selectDir = component.selectFolder,
                            deselectDir = component.deselectDir,
                            selectFiles = component.selectFiles,
                            addGame = {
                                component.downloadNewEntry(it)
                                component.onDismissed()
                            },
                            onDismiss = component.onDismissed,
                            isAddLocalGame = false,
                            buttonText = stringResource(R.string.download_button_description)
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
                                isError = config.isError,
                                initialValue = "${
                                    when (it) {
                                        InputState.INPUT_IMAGE_WIDTH -> settings.imageWidth
                                        InputState.INPUT_IMAGE_HEIGHT -> settings.imageHeight
                                        else -> ""
                                    }
                                }",
                                supportValue = config.supportValue,
                                supportErrorValue = config.supportErrorValue,
                                label = {
                                    Text(
                                        when (it) {
                                            InputState.INPUT_IMAGE_WIDTH -> stringResource(R.string.input_width_title)
                                            InputState.INPUT_IMAGE_HEIGHT -> stringResource(R.string.input_height_title)
                                            InputState.INPUT_WITH_MESSAGE_EXEC -> stringResource(R.string.input_msg_exec_title)
                                            else -> stringResource(R.string.input_generic_title)
                                        }
                                    )
                                },
                                inputValue = { s -> component.inputValue(it to s) },
                                keyboardOptions =
                                    when (it) {
                                        InputState.INPUT_IMAGE_WIDTH, InputState.INPUT_IMAGE_HEIGHT ->
                                            KeyboardOptions(keyboardType = KeyboardType.Number)

                                        else -> KeyboardOptions()
                                    },
                                cleanError = component.onCleanError,
                                onDismiss = component.onDismissed
                            )
                        }
                    }

                    DialogsState.DIALOG_OUTPUT_VALUE -> config.outputState.let {
                        if (it == null || config.outputValue.isBlank()) {
                            component.onDismissed()
                        } else {
                            if (it != OutputState.OUTPUT_LIST) {
                                DialogOutputValueContent(
                                    message = config.outputValue,
                                    isImage = it == OutputState.OUTPUT_IMAGE,
                                    onConfirm = { component.confirmMessage(it to 0) },
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
                                    onConfirm = { i -> component.confirmMessage(it to i) },
                                    onDismiss = component.onDismissed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}