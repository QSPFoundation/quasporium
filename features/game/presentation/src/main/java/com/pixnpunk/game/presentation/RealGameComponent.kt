package com.pixnpunk.game.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.child
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dialogs.presentation.DialogsComponent
import com.pixnpunk.dialogs.presentation.DialogsConfig
import com.pixnpunk.dialogs.presentation.DialogsState
import com.pixnpunk.dialogs.presentation.RealDialogsComponent
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.LibGenItem
import com.pixnpunk.dto.LibTypeDialog
import com.pixnpunk.dto.OutputState
import com.pixnpunk.extra.presentation.RealExtraComponent
import com.pixnpunk.game.presentation.mvi.GameStore
import com.pixnpunk.game.presentation.mvi.RealGameStore
import com.pixnpunk.main.presentation.RealMainComponent
import com.pixnpunk.natives.SupervisorViewModel
import com.pixnpunk.`object`.presentation.RealObjectComponent
import com.pixnpunk.utils.Base64Util.decodeBase64
import com.pixnpunk.utils.Base64Util.isBase64
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.HtmlUtil.isContainsHtmlTags
import com.pixnpunk.utils.HtmlUtil.removeHtmlTags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.io.FileNotFoundException
import kotlin.contracts.ExperimentalContracts

class RealGameComponent(
    private val componentContext: ComponentContext,
    private val onStartActivity: (Intent) -> Unit
) : ComponentContext by componentContext, GameComponent, KoinComponent {

    private val supervisorService: SupervisorViewModel by inject()

    private var rootDir: DocumentFile? = null

    private val navigation = StackNavigation<ChildConfig>()
    private val dialogNavigation = SlotNavigation<DialogsConfig>()
    private val store = instanceKeeper.getStore {
        RealGameStore(DefaultStoreFactory()).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<GameStore.State> = store.stateFlow
    override val label = store.labels
    override val childStack: Value<ChildStack<*, GameComponent.Child>> = childStack(
        source = navigation,
        serializer = serializer(),
        initialConfiguration = ChildConfig.Main,
        childFactory = ::createChild
    )
    override val dialogSlot: Value<ChildSlot<*, DialogsComponent>> =
        childSlot(
            source = dialogNavigation,
            serializer = DialogsConfig.serializer()
        ) { config, context ->
            RealDialogsComponent(
                componentContext = context,
                dialogsConfig = config,
                onCleanError = ::onCleanError,
                onEnterValue = { onEnterValue(it.first, it.second) },
                onExecValue = { onExecValue(it) },
                onSelMenuItem = { onSelMenuItem(it) },
                onDismissed = dialogNavigation::dismiss,
                addNewEntry = { },
                editEntry = { },
                downloadNewEntry = { },
                selectFolder = { },
                selectFile = { },
                deselectFile = { },
                selectColor = { },
                confirmMessage = { },
                inputValue = { },
                doPlayGame = { _, _ -> },
                deselectDir = { },
                selectFiles = { }
            )
        }

    fun onSaveFileResult(fileUri: Uri) {
        store.accept(GameStore.Intent.OnSaveFile(fileUri))
    }

    fun onLoadFileResult(fileUri: Uri) {
        store.accept(GameStore.Intent.OnLoadFile(fileUri))
    }

    override fun doCreateSaveIntent() {
        store.accept(GameStore.Intent.CreateSaveIntent)
    }

    override fun doCreateLoadIntent() {
        store.accept(GameStore.Intent.CreateLoadIntent)
    }

    private fun onSelMenuItem(index: Int) {
        store.accept(GameStore.Intent.OnSelectMenuItem(index))
    }

    private fun onEnterValue(inputString: String, isBox: Boolean) {
        store.accept(GameStore.Intent.OnEnterValue(inputString, isBox))
    }

    private fun onExecValue(inputString: String) {
        store.accept(GameStore.Intent.OnExecCode(inputString))
    }

    private fun onCleanError() {
        val oldConfig = dialogSlot.child?.configuration as DialogsConfig
        dialogNavigation.activate(
            configuration = oldConfig.copy(
                isDialogInputError = false,
                dialogInputErrorString = ""
            )
        )
    }

    @OptIn(DelicateDecomposeApi::class)
    override fun navigation(child: ChildConfig) {
        navigation.bringToFront(child)
    }

    override fun doShowDialog() {
        val currentState = store.state
        if (currentState.isInputEnabled) {
            doShowDialog(
                inputString = "Input something",
                dialogState = LibTypeDialog.DIALOG_INPUT
            )
        } else if (currentState.isExecutorEnabled) {
            doShowDialog(
                inputString = "QSP Terminal",
                dialogState = LibTypeDialog.DIALOG_INPUT
            )
        }
    }

    override fun doShowDialog(
        inputString: String,
        dialogState: LibTypeDialog,
        inputListItems: List<LibGenItem>?
    ) {
        when (dialogState) {
            LibTypeDialog.DIALOG_MESSAGE -> {
                val oldConfig = dialogSlot.child?.configuration as? DialogsConfig
                if (oldConfig != null && oldConfig.dialogsState == DialogsState.DIALOG_OUTPUT_VALUE) {
                    dialogNavigation.activate(
                        configuration = oldConfig.copy(
                            isDialogInputError = true,
                            dialogInputErrorString = inputString
                        )
                    )
                } else {
                    dialogNavigation.activate(
                        DialogsConfig(
                            dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                            outputValue = inputString,
                            outputState = OutputState.OUTPUT_SIMPLE_MSG
                        )
                    )
                }
            }

            LibTypeDialog.DIALOG_ERROR -> {
                dialogNavigation.activate(
                    configuration = DialogsConfig(
                        dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                        outputState = OutputState.OUTPUT_SIMPLE_MSG,
                        outputValue = inputString
                    )
                )
            }

            LibTypeDialog.DIALOG_INPUT ->
                dialogNavigation.activate(
                    configuration = DialogsConfig(
                        dialogsState = DialogsState.DIALOG_INPUT_VALUE,
                        inputState = InputState.INPUT_WITH_MESSAGE,
                        outputValue = inputString
                    )
                )


            LibTypeDialog.DIALOG_PICTURE -> {
                dialogNavigation.activate(
                    configuration = DialogsConfig(
                        dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                        outputState = OutputState.OUTPUT_IMAGE,
                        outputValue = inputString
                    )
                )
            }
            LibTypeDialog.DIALOG_MENU ->
                dialogNavigation.activate(
                    configuration = DialogsConfig(
                        dialogsState = DialogsState.DIALOG_OUTPUT_VALUE,
                        outputState = OutputState.OUTPUT_LIST,
                        menuItems = inputListItems ?: emptyList()
                    )
                )
        }
    }

    override fun runGame(
        gameId: Long,
        gameTitle: String,
        gameDirUri: Uri,
        gameFileUri: Uri
    ) {
        println(gameDirUri)
        supervisorService.startService(gameId, gameTitle, gameDirUri, gameFileUri)

        rootDir = gameDirUri.toDocumentFile(get())

        store.accept(GameStore.Intent.StartGameDialogFlow(rootDir))
    }

    override fun restartGame() {
        supervisorService.onRestartGame()
    }

    private inner class GameWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            val uriScheme = request.url.scheme ?: return false
            val uriDecode = Uri.decode(request.url.toString())

            when (uriScheme) {
                "exec" ->
                    uriDecode.substring(5)
                        .let {
                            if (it.isBase64()) {
                                it.decodeBase64()
                            } else {
                                it
                            }
                        }
                        .let {
                            if (it.isContainsHtmlTags()) {
                                onExecValue(it.removeHtmlTags())
                            } else {
                                onExecValue(it)
                            }
                        }

                "https", "http" ->
                    Intent(
                        Intent.ACTION_VIEW,
                        uriDecode.toUri()
                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }.also {
                        try {
                            onStartActivity(it)
                        } catch (e: ActivityNotFoundException) {
//                            Firebase.crashlytics.log("shouldOverrideUrlLoading: $e")
                        }
                    }

                "file" ->
                    Intent(
                        Intent.ACTION_VIEW,
                        uriScheme.replace("file:/", "https:").toUri()
                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }.also {
                        try {
                            onStartActivity(it)
                        } catch (e: ActivityNotFoundException) {
//                            Firebase.crashlytics.log("shouldOverrideUrlLoading: $e")
                        }
                    }
            }
            return true
        }

        @OptIn(ExperimentalContracts::class)
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            val uriScheme = request.url.scheme ?: return super.shouldInterceptRequest(view, request)
            if (!uriScheme.startsWith("file")) return super.shouldInterceptRequest(view, request)
            val mRootDir = rootDir
            if (!mRootDir.isWritableDir()) return super.shouldInterceptRequest(view, request)

            return runCatching {
                val uriPath = request.url.path ?: throw NullPointerException()
                val mediaFile = mRootDir.child(get(), uriPath)
                if (mediaFile.isWritableFile()) {
                    WebResourceResponse(
                        MimeType.getMimeTypeFromFileName(mediaFile.name),
                        null,
                        get<Context>().contentResolver.openInputStream(mediaFile.uri)
                    )
                } else {
                    throw FileNotFoundException()
                }
            }.getOrElse {
//                if (getSettingsController().isUseImageDebug) {
//                    doShowErrorDialog(uri.getPath(), ErrorType.IMAGE_ERROR)
//                }
//                Firebase.crashlytics.log("shouldOverrideUrlLoading: ERROR! | Cause: $it")
                super.shouldInterceptRequest(view, request)
            }
        }
    }


    private fun createChild(
        config: ChildConfig,
        componentContext: ComponentContext
    ): GameComponent.Child = when (config) {
        is ChildConfig.Main -> {
            GameComponent.Child.MainChild(
                RealMainComponent(
                    componentContext = componentContext,
                    gameWebViewClient = GameWebViewClient(),
                )
            )
        }

        is ChildConfig.Extra -> {
            GameComponent.Child.ExtraChild(
                RealExtraComponent(
                    componentContext = componentContext,
                    gameWebViewClient = GameWebViewClient(),
                )
            )
        }

        is ChildConfig.Object -> {
            GameComponent.Child.ObjectChild(
                RealObjectComponent(
                    componentContext = componentContext
                )
            )
        }
    }

    @Serializable
    sealed class ChildConfig {
        @Serializable
        data object Main : ChildConfig()

        @Serializable
        data object Extra : ChildConfig()

        @Serializable
        data object Object : ChildConfig()
    }
}