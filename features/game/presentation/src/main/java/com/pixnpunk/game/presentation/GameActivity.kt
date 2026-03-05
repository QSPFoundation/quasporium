package com.pixnpunk.game.presentation

import android.content.Intent
import android.os.Bundle
import android.os.Process.killProcess
import android.os.Process.myPid
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.arkivanov.decompose.defaultComponentContext
import com.pixnpunk.audio.AudioPlayerViewModel
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.presentation.mvi.GameStore
import com.pixnpunk.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import org.koin.android.ext.android.inject

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
    private var gameDirUriBox: String = ""
    private var gameFileUriBox: String = ""

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

        intent?.let {
            gameId = it.getLongExtra("gameId", 1L)
            gameTitle = it.getStringExtra("gameTitle") ?: "Title"
            gameDirUriBox = it.getStringExtra("gameDirUri") ?: ""
            gameFileUriBox = it.getStringExtra("gameFileUri") ?: ""
        }

        root = RealGameComponent(
            componentContext = defaultComponentContext(),
            onStartActivity = {
                startActivity(it)
            },
            gameId = gameId,
            gameTitle = gameTitle,
            gameDirUriBox = gameDirUriBox,
            gameFileUriBox = gameFileUriBox
        )

        root.runGame(settingsState.value.nativeLibFrom)

        setContent {
            LaunchedEffect(Unit) {
                root.label.collect {
                    when (it) {
                        is GameStore.Label.ShowDialog ->
                            root.doShowDialog(
                                it.dialogState,
                                it.inputString,
                                it.inputListItems ?: emptyList()
                            )

                        is GameStore.Label.ShowLoadFilePicker ->
                            requestOpenFile.launch(it.intent)

                        is GameStore.Label.ShowSaveFilePicker ->
                            requestCreateFile.launch(it.intent)
                    }
                }
            }

            GameContent(
                component = root,
                settings = settingsState.collectAsState().value,
                onFinish = { killProcess(myPid()) }
            )
        }
    }
}