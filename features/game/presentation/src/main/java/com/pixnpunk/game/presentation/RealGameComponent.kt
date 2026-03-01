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
import androidx.webkit.WebViewAssetLoader
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.MimeType
import com.anggrayudi.storage.file.child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.child
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
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
import com.pixnpunk.dto.NativeLibAuthors
import com.pixnpunk.dto.OutputState
import com.pixnpunk.extra.presentation.RealExtraComponent
import com.pixnpunk.game.presentation.mvi.GameStore
import com.pixnpunk.game.presentation.mvi.RealGameStore
import com.pixnpunk.main.presentation.MainComponent
import com.pixnpunk.main.presentation.RealMainComponent
import com.pixnpunk.natives.NativeViewModel
import com.pixnpunk.`object`.presentation.RealObjectComponent
import com.pixnpunk.utils.Base64Util.decodeBase64
import com.pixnpunk.utils.Base64Util.isBase64
import com.pixnpunk.utils.FileUtil.isWritableDir
import com.pixnpunk.utils.FileUtil.isWritableFile
import com.pixnpunk.utils.HtmlUtil.isContainsHtmlTags
import com.pixnpunk.utils.HtmlUtil.removeHtmlTags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
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

    private val supervisorService: NativeViewModel by inject()

    private var rootDir: DocumentFile? = null

    private val dialogNavigation = SlotNavigation<DialogsConfig>()
    private val extraNavigation = SlotNavigation<Unit>()
    private val objectNavigation = SlotNavigation<Unit>()

    private val store = instanceKeeper.getStore {
        RealGameStore(DefaultStoreFactory()).create()
    }

    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/game_files/", ExternalStoragePathHandler())
        .build()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<GameStore.State> = store.stateFlow
    override val label = store.labels

    override val dialogSlot: Value<ChildSlot<*, DialogsComponent>> =
        childSlot(
            source = dialogNavigation,
            serializer = DialogsConfig.serializer()
        ) { config, context ->
            RealDialogsComponent(
                componentContext = context,
                dialogsConfig = config,
                onCleanError = ::onCleanError,
                inputValue = ::onInputValue,
                confirmMessage = ::onConfirmMessage,
                onDismissed = dialogNavigation::dismiss
            )
        }

    override val extraSlot: Value<ChildSlot<*, RealExtraComponent>> =
        childSlot(
            key = "ExtraChildStack",
            source = extraNavigation,
            serializer = serializer(),
            handleBackButton = true
        ) { _, ctx ->
            RealExtraComponent(
                componentContext = ctx,
                gameWebViewClient = GameWebViewClient()
            )
        }

    override val objectSlot: Value<ChildSlot<*, RealObjectComponent>> =
        childSlot(
            key = "ObjectChildStack",
            source = objectNavigation,
            serializer = serializer(),
            handleBackButton = true
        ) { _, ctx ->
            RealObjectComponent(
                componentContext = ctx
            )
        }

    override val mainComponent: MainComponent = RealMainComponent(
        componentContext = childContext("MainComponent"),
        gameWebViewClient = GameWebViewClient()
    )

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

    private fun onConfirmMessage(msg: Pair<OutputState, Int>) {
        when (msg.first) {
            OutputState.OUTPUT_LIST ->
                store.accept(GameStore.Intent.OnSelectMenuItem(msg.second))

            else -> {}
        }
    }

    private fun onInputValue(msg: Pair<InputState, String>) {
        store.accept(
            GameStore.Intent.OnInputValue(msg.first, msg.second)
        )
    }

    private fun onExecValue(inputString: String) {
        store.accept(GameStore.Intent.OnExecCode(inputString))
    }

    private fun onCleanError() {
        val oldConfig = dialogSlot.child?.configuration as DialogsConfig
        dialogNavigation.activate(
            configuration = oldConfig.copy(
                isError = false,
                supportErrorValue = ""
            )
        )
    }

    override fun doShowDialog() {
        val currentState = store.state
        if (currentState.isInputEnabled) {
            doShowDialog(
                dialogState = LibTypeDialog.DIALOG_INPUT_AREA
            )
        } else if (currentState.isExecutorEnabled) {
            doShowDialog(
                dialogState = LibTypeDialog.DIALOG_INPUT_EXEC
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
                            isError = true,
                            supportErrorValue = inputString
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

            LibTypeDialog.DIALOG_INPUT_BOX,
            LibTypeDialog.DIALOG_INPUT_AREA,
            LibTypeDialog.DIALOG_INPUT_EXEC ->
                dialogNavigation.activate(
                    configuration = DialogsConfig(
                        dialogsState = DialogsState.DIALOG_INPUT_VALUE,
                        inputState =
                            when (dialogState) {
                                LibTypeDialog.DIALOG_INPUT_BOX -> InputState.INPUT_WITH_MESSAGE_BOX
                                LibTypeDialog.DIALOG_INPUT_AREA -> InputState.INPUT_WITH_MESSAGE_AREA
                                else -> InputState.INPUT_WITH_MESSAGE_EXEC
                            },
                        supportValue = inputString
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

    override fun stateSwitchExtraSlot() {
        extraNavigation.dismiss()
        objectNavigation.dismiss()
        extraNavigation.activate(Unit)
        store.accept(GameStore.Intent.MuteExtraUpdate)
    }

    override fun stateSwitchObjectSlot() {
        extraNavigation.dismiss()
        objectNavigation.dismiss()
        objectNavigation.activate(Unit)
        store.accept(GameStore.Intent.MuteObjectUpdate)
    }

    override fun clearAllSlot() {
        extraNavigation.dismiss()
        objectNavigation.dismiss()
        store.accept(GameStore.Intent.MuteMainUpdate)
    }

    override fun runGame(
        gameId: Long,
        gameTitle: String,
        gameDirUri: Uri,
        gameFileUri: Uri,
        nativeLibFrom: NativeLibAuthors
    ) {
        supervisorService.startService(
            gameId, gameTitle,
            gameDirUri, gameFileUri,
            nativeLibFrom
        )

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
                        } catch (_: ActivityNotFoundException) {

                        }
                    }

                "file" ->
                    Intent(
                        Intent.ACTION_VIEW,
                        uriScheme.replace("file:/", "https:").toUri()
                    ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }.also {
                        try {
                            onStartActivity(it)
                        } catch (_: ActivityNotFoundException) {

                        }
                    }
            }
            return true
        }

        @OptIn(ExperimentalContracts::class)
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ) = runCatching {
            val uriPath = request.url ?: throw NullPointerException()
            assetLoader.shouldInterceptRequest(uriPath)
        }.getOrElse { super.shouldInterceptRequest(view, request) }
    }

    private inner class ExternalStoragePathHandler() : WebViewAssetLoader.PathHandler {
        @OptIn(ExperimentalContracts::class)
        override fun handle(path: String): WebResourceResponse? {
            val mRootDir = rootDir
            if (!mRootDir.isWritableDir()) return null
            return runCatching {
                val mediaFile = mRootDir.child(get(), path)
                if (!mediaFile.isWritableFile()) {
                    throw FileNotFoundException()
                } else {
                    WebResourceResponse(
                        MimeType.getMimeTypeFromFileName(mediaFile.name),
                        null,
                        200,
                        "OK",
                        mapOf(
                            "Cache-Control" to "public, max-age=3600, immutable",
                            "Access-Control-Allow-Origin" to "*"
                        ),
                        get<Context>().contentResolver.openInputStream(mediaFile.uri)
                    )
                }
            }.getOrElse { null }
        }
    }
}