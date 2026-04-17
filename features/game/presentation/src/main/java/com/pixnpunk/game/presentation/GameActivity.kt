package com.pixnpunk.game.presentation

import android.os.Bundle
import android.os.Process.killProcess
import android.os.Process.myPid
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.anggrayudi.storage.contract.FileCreationContract
import com.anggrayudi.storage.contract.FileCreationResult
import com.anggrayudi.storage.contract.FilePickerResult
import com.anggrayudi.storage.contract.OpenFilePickerContract
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.FileFullPath
import com.anggrayudi.storage.file.MimeType.BINARY_FILE
import com.anggrayudi.storage.file.getAbsolutePath
import com.arkivanov.decompose.defaultComponentContext
import com.pixnpunk.game.presentation.mvi.GameStore
import com.pixnpunk.settings.data.repository.SettingsRepository
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class GameActivity : ComponentActivity() {

    private lateinit var rootComponent: RealGameComponent

    private val settingsRepository: SettingsRepository by inject { parametersOf(gameId) }

    private val requestCreateFile =
        registerForActivityResult(
            FileCreationContract(get())
        ) {
            runCatching {
                when (it) {
                    is FileCreationResult.StoragePermissionDenied -> {
                        rootComponent.onFileCorruptResult()
                    }

                    is FileCreationResult.CanceledByUser -> {
                        rootComponent.onFileCorruptResult(true)
                    }

                    is FileCreationResult.Created -> {
                        rootComponent.onSaveFileResult(it.file.uri)
                    }
                }
            }.onFailure { rootComponent.onFileCorruptResult() }
        }

    private val requestOpenFile =
        registerForActivityResult(
            OpenFilePickerContract(get())
        ) {
            runCatching {
                when (it) {
                    is FilePickerResult.StoragePermissionDenied -> {
                        rootComponent.onFileCorruptResult()
                    }

                    is FilePickerResult.CanceledByUser -> {
                        rootComponent.onFileCorruptResult(true)
                    }

                    is FilePickerResult.Picked -> {
                        rootComponent.onLoadFileResult(it.files.first().uri)
                    }
                }
            }.onFailure { rootComponent.onFileCorruptResult() }
        }

    private val gameId: Long by lazy { intent.getLongExtra("gameId", -1L) }
    private val gameTitle: String by lazy { intent.getStringExtra("gameTitle") ?: "Title" }
    private val gameDirUriBox: String by lazy { intent.getStringExtra("gameDirUri") ?: "" }
    private val gameFileUriBox: String by lazy { intent.getStringExtra("gameFileUri") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (settingsRepository.currentSettings.isImmersModeEnabled) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }

        rootComponent = RealGameComponent(
            componentContext = defaultComponentContext(),
            onFinishActivity = { killProcess(myPid()) },
            gameId = gameId,
            gameTitle = gameTitle,
            gameDirUriBox = gameDirUriBox,
            gameFileUriBox = gameFileUriBox
        )

        rootComponent.doRunGame()

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                rootComponent.audioPlayer.resumeAll()
            }

            override fun onPause(owner: LifecycleOwner) {
                rootComponent.audioPlayer.pauseAll()
            }
        })

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                rootComponent.label.collect {
                    when (it) {
                        is GameStore.Label.ShowDialog ->
                            rootComponent.doShowDialog(
                                it.dialogState,
                                it.inputString,
                                it.inputListItems ?: emptyList()
                            )

                        is GameStore.Label.ShowListSavesDialog ->
                            rootComponent.doShowDialog(it.inputListItems)

                        is GameStore.Label.ShowSnackBar ->
                            snackbarHostState.showSnackbar(it.inputString)

                        is GameStore.Label.ShowLoadFilePicker ->
                            runCatching {
                                val rootDir =
                                    gameDirUriBox.toUri().toDocumentFile(this@GameActivity)!!
                                requestOpenFile.launch(
                                    OpenFilePickerContract.Options(
                                        initialPath = FileFullPath(
                                            context = this@GameActivity,
                                            fullPath = rootDir.getAbsolutePath(this@GameActivity)
                                        ),
                                        filterMimeTypes = setOf(BINARY_FILE)
                                    )
                                )
                            }

                        is GameStore.Label.ShowSaveFilePicker ->
                            runCatching {
                                val rootDir =
                                    gameDirUriBox.toUri().toDocumentFile(this@GameActivity)!!
                                requestCreateFile.launch(
                                    FileCreationContract.Options(
                                        mimeType = BINARY_FILE,
                                        fileName = "${(UInt.MIN_VALUE..UInt.MAX_VALUE).random()}.sav",
                                        initialPath = FileFullPath(
                                            context = this@GameActivity,
                                            fullPath = rootDir.getAbsolutePath(this@GameActivity)
                                        )
                                    )
                                )
                            }
                    }
                }
            }

            GameContent(
                component = rootComponent,
                snackbarHostState = snackbarHostState
            )
        }
    }
}