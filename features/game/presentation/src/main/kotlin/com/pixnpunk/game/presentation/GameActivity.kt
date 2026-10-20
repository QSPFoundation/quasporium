package com.pixnpunk.game.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.retainedComponent
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.settings.domain.repository.GameSettingsPreloader
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get

class GameActivity : ComponentActivity() {

    private lateinit var rootComponent: RealGameComponent

    private val isImmersiveEnabled: Boolean
        get() = rootComponent.settingsState.value.isImmersModeEnabled

    private val pendingComponentActions = mutableListOf<(RealGameComponent) -> Unit>()

    private fun withComponent(action: (RealGameComponent) -> Unit) {
        if (::rootComponent.isInitialized) action(rootComponent) else pendingComponentActions += action
    }

    private val requestCreateFile =
        registerForActivityResult(CreateSaveFileContract()) { uri ->
            withComponent { it.onSaveFilePicked(uri?.let { path -> VfsPath(path.toString()) }) }
        }

    private val requestOpenFile =
        registerForActivityResult(OpenSaveFileContract()) { uri ->
            withComponent { it.onLoadFilePicked(uri?.let { path -> VfsPath(path.toString()) }) }
        }

    private val gameId: Long by lazy { intent.getLongExtra("gameId", -1L) }
    private val gameTitle: String by lazy { intent.getStringExtra("gameTitle") ?: "Title" }
    private val gameDirUriBox: String by lazy { intent.getStringExtra("gameDirUri") ?: "" }
    private val gameFileUriBox: String by lazy { intent.getStringExtra("gameFileUri") ?: "" }
    private val gameSaveUriBox: String by lazy { intent.getStringExtra("gameSaveUri") ?: "" }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && ::rootComponent.isInitialized && isImmersiveEnabled) {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                hide(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            get<GameSettingsPreloader>().preloadGameSettings(gameId)
            setUpGame()
        }
    }

    private fun setUpGame() {
        rootComponent = retainedComponent {
            RealGameComponent(
                componentContext = it,
                gameId = gameId,
                gameTitle = gameTitle,
                gameDirUriBox = gameDirUriBox,
                gameFileUriBox = gameFileUriBox,
                gameSaveUriBox = gameSaveUriBox
            )
        }
        pendingComponentActions.forEach { it(rootComponent) }
        pendingComponentActions.clear()

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (isImmersiveEnabled) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                rootComponent.setGameVisible(true)
            }

            override fun onStop(owner: LifecycleOwner) {
                rootComponent.setGameVisible(false)
            }

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

            if (state.isFinishRequested) {
                LaunchedEffect(Unit) { finish() }
            }

            LaunchedEffect(state.isUserChooseSave) {
                if (state.isUserChooseSave) {
                    runCatching {
                        requestCreateFile.launch(
                            SaveFileRequest(
                                fileName = "${(UInt.MIN_VALUE..UInt.MAX_VALUE).random()}.sav",
                                initialUri = initialFolderUri(gameDirUriBox)
                            )
                        )
                    }.onFailure { rootComponent.onSaveFilePicked(null) }
                }
            }

            LaunchedEffect(state.isUserChooseLoad) {
                if (state.isUserChooseLoad) {
                    runCatching {
                        requestOpenFile.launch(initialFolderUri(gameDirUriBox))
                    }.onFailure { rootComponent.onLoadFilePicked(null) }
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
