package com.pixnpunk.game.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process.killProcess
import android.os.Process.myPid
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.anggrayudi.storage.extension.toDocumentFile
import com.arkivanov.decompose.defaultComponentContext
import com.pixnpunk.audio.AudioPlayerViewModel
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.presentation.mvi.GameStore
import com.pixnpunk.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import androidx.compose.runtime.collectAsState

class GameActivity : ComponentActivity() {
    private lateinit var root: RealGameComponent

    private val audioPlayer: AudioPlayerViewModel by inject()
    private val settingsRepo: SettingsRepository by inject()
    private val settingsState: StateFlow<SettingsEntity> =
        settingsRepo.settingsFlow.stateIn(
            scope = CoroutineScope(Dispatchers.Main),
            started = SharingStarted.Eagerly,
            initialValue = settingsRepo.currentSettings
        )

    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    private val requestCreateFile: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { root.onSaveFileResult(it) }
            }
        }
    private val requestOpenFile: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { root.onLoadFileResult(it) }
            }
        }

    private var gameId: Long = 1L
    private var gameTitle: String = "Title"
    private var gameDirUri: Uri = Uri.EMPTY
    private var gameFileUri: Uri = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(audioPlayer)

        if (settingsState.value.isImmersModeEnabled) {
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                ViewCompat.onApplyWindowInsets(view, windowInsets)
            }
        }

        if (savedInstanceState != null) {
            val dirUri = savedInstanceState.getString("fileUri")
            if (dirUri != null) {
                gameDirUri = dirUri.toUri()
            }

            val fileUri = savedInstanceState.getString("fileUri")
            if (fileUri != null) {
                gameFileUri = fileUri.toUri()
            }
        } else {
            intent?.let {
                gameId = it.getLongExtra("gameId", 1L)
                gameTitle = it.getStringExtra("gameTitle") ?: "Title"
                gameDirUri = it.getStringExtra("gameDirUri")?.toUri() ?: Uri.EMPTY
                gameFileUri = it.getStringExtra("gameFileUri")?.toUri() ?: Uri.EMPTY
            }
        }

        root = RealGameComponent(
            componentContext = defaultComponentContext(),
            onStartActivity = {
                startActivity(it)
            }
        )

        val gameDirFile = gameDirUri.toDocumentFile(this)
        if (gameDirFile != null) {
            if (!root.model.value.isGameRunning) {
                root.runGame(
                    gameId, gameTitle,
                    gameDirUri, gameFileUri,
                    settingsState.value.nativeLibFrom
                )
            }
        }

        mainScope.launch {
            root.label.collect {
                when (it) {
                    is GameStore.Label.ShowDialog ->
                        root.doShowDialog(it.inputString, it.dialogState)

                    is GameStore.Label.ShowLoadFilePicker -> requestOpenFile.launch(it.intent)

                    is GameStore.Label.ShowSaveFilePicker -> requestCreateFile.launch(it.intent)
                }
            }
        }

        setContent {
            GameContent(
                component = root,
                settings = settingsState.collectAsState().value,
                onFinish = { killProcess(myPid()) }
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("dirUri", gameDirUri.toString())
        outState.putString("fileUri", gameFileUri.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        mainScope.cancel()
        super.onDestroy()
    }
}