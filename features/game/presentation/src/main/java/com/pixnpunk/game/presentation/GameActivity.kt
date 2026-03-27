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
import com.anggrayudi.storage.contract.FileCreationContract
import com.anggrayudi.storage.contract.FileCreationResult
import com.anggrayudi.storage.contract.FilePickerResult
import com.anggrayudi.storage.contract.OpenFilePickerContract
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.FileFullPath
import com.anggrayudi.storage.file.MimeType.BINARY_FILE
import com.anggrayudi.storage.file.getAbsolutePath
import com.arkivanov.decompose.defaultComponentContext
import com.pixnpunk.audio.AudioPlayerViewModel
import com.pixnpunk.game.presentation.mvi.GameStore
import com.pixnpunk.settings.data.repository.SettingsRepository
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class GameActivity : ComponentActivity() {

    private lateinit var root: RealGameComponent

    private val audioPlayer: AudioPlayerViewModel by inject()
    private val settingsRepository: SettingsRepository by inject { parametersOf(gameId) }

    private val requestCreateFile =
        registerForActivityResult(
            FileCreationContract(get())
        ) {
            runCatching {
                when (it) {
                    is FileCreationResult.StoragePermissionDenied -> {
                        root.onFileCorruptResult()
                    }

                    is FileCreationResult.CanceledByUser -> {
                        root.onFileCorruptResult(true)
                    }

                    is FileCreationResult.Created -> {
                        root.onSaveFileResult(it.file.uri)
                    }
                }
            }.onFailure { root.onFileCorruptResult() }
        }

    private val requestOpenFile =
        registerForActivityResult(
            OpenFilePickerContract(get())
        ) {
            runCatching {
                when (it) {
                    is FilePickerResult.StoragePermissionDenied -> {
                        root.onFileCorruptResult()
                    }

                    is FilePickerResult.CanceledByUser -> {
                        root.onFileCorruptResult(true)
                    }

                    is FilePickerResult.Picked -> {
                        root.onLoadFileResult(it.files.first().uri)
                    }
                }
            }.onFailure { root.onFileCorruptResult() }
        }

    private val gameId: Long by lazy { intent.getLongExtra("GAME_ID", -1L) }
    private val gameTitle: String by lazy { intent.getStringExtra("gameTitle") ?: "Title" }
    private val gameDirUriBox: String by lazy { intent.getStringExtra("gameDirUri") ?: "" }
    private val gameFileUriBox: String by lazy { intent.getStringExtra("gameFileUri") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(audioPlayer)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (settingsRepository.currentSettings.isImmersModeEnabled) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }

        root = RealGameComponent(
            componentContext = defaultComponentContext(),
            onStartActivity = { startActivity(it) },
            onFinishActivity = { killProcess(myPid()) },
            gameId = gameId,
            gameTitle = gameTitle,
            gameDirUriBox = gameDirUriBox,
            gameFileUriBox = gameFileUriBox
        )

        root.doRunGame()

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                root.label.collect {
                    when (it) {
                        is GameStore.Label.ShowDialog ->
                            root.doShowDialog(
                                it.dialogState,
                                it.inputString,
                                it.inputListItems ?: emptyList()
                            )

                        is GameStore.Label.ShowListSavesDialog ->
                            root.doShowDialog(it.inputListItems)

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
                component = root,
                snackbarHostState = snackbarHostState
            )
        }
    }
}