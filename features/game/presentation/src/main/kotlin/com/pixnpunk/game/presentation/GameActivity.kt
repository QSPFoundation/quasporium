package com.pixnpunk.game.presentation

import android.os.Bundle
import android.os.Process.killProcess
import android.os.Process.myPid
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anggrayudi.storage.contract.FileCreationContract
import com.anggrayudi.storage.contract.FileCreationResult
import com.anggrayudi.storage.contract.FilePickerResult
import com.anggrayudi.storage.contract.OpenFilePickerContract
import com.anggrayudi.storage.extension.toDocumentFile
import com.anggrayudi.storage.file.FileFullPath
import com.anggrayudi.storage.file.MimeType.BINARY_FILE
import com.anggrayudi.storage.file.getAbsolutePath
import com.arkivanov.decompose.retainedComponent
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.settings.dto.repository.SettingsRepository
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class GameActivity : ComponentActivity() {

    private lateinit var rootComponent: RealGameComponent

    private val settingsRepository: SettingsRepository by inject()

    private val requestCreateFile =
        registerForActivityResult(
            FileCreationContract(get())
        ) {
            runCatching {
                when (it) {
                    is FileCreationResult.StoragePermissionDenied -> {
                        rootComponent.onSaveFilePicked(null)
                    }

                    is FileCreationResult.CanceledByUser -> {
                        rootComponent.onSaveFilePicked(null)
                    }

                    is FileCreationResult.Created -> {
                        rootComponent.onSaveFilePicked(VfsPath(it.file.uri.toString()))
                    }
                }
            }.onFailure { rootComponent.onSaveFilePicked(null) }
        }

    private val requestOpenFile =
        registerForActivityResult(
            OpenFilePickerContract(get())
        ) {
            runCatching {
                when (it) {
                    is FilePickerResult.StoragePermissionDenied -> {
                        rootComponent.onLoadFilePicked(null)
                    }

                    is FilePickerResult.CanceledByUser -> {
                        rootComponent.onLoadFilePicked(null)
                    }

                    is FilePickerResult.Picked -> {
                        rootComponent.onLoadFilePicked(
                            VfsPath(
                                it.files.first().uri.toString()
                            )
                        )
                    }
                }
            }.onFailure { rootComponent.onLoadFilePicked(null) }
        }

    private val gameId: Long by lazy { intent.getLongExtra("gameId", -1L) }
    private val gameTitle: String by lazy { intent.getStringExtra("gameTitle") ?: "Title" }
    private val gameDirUriBox: String by lazy { intent.getStringExtra("gameDirUri") ?: "" }
    private val gameFileUriBox: String by lazy { intent.getStringExtra("gameFileUri") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (settingsRepository.settingsFlow.value.isImmersModeEnabled) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }

        rootComponent = retainedComponent {
            RealGameComponent(
                componentContext = it,
                libAuthors = settingsRepository.settingsFlow.value.nativeLibFrom,
                onFinishActivity = { killProcess(myPid()) },
                gameId = gameId,
                gameTitle = gameTitle,
                gameDirUriBox = gameDirUriBox,
                gameFileUriBox = gameFileUriBox
            )
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                rootComponent.audioPlayer.resumeAll()
            }

            override fun onPause(owner: LifecycleOwner) {
                rootComponent.audioPlayer.pauseAll()
            }
        })

        setContent {
            val state by rootComponent.model.collectAsStateWithLifecycle()

            val snackbarHostState = remember { SnackbarHostState() }

            if (state.isUserChooseSave) {
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

            if (state.isUserChooseLoad) {
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
            }

            GameContent(
                component = rootComponent,
                state = state,
                snackbarHostState = snackbarHostState
            )
        }
    }
}
