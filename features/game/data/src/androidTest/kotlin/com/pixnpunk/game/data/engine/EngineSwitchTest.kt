package com.pixnpunk.game.data.engine

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pixnpunk.game.data.TestGame
import com.pixnpunk.game.data.engine.jni.NativeEngineBridge
import com.pixnpunk.game.data.engine.jni.QspEngineBridgeImpl
import com.pixnpunk.game.data.engine.jni.QspLegacyEngineBridgeImpl
import com.pixnpunk.game.data.vfs.impl.FileSystemProviderImpl
import com.pixnpunk.game.domain.model.EngineDialog
import com.pixnpunk.game.domain.model.GameState
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.game.domain.repository.AudioPlayerInteractor
import com.pixnpunk.game.domain.repository.EngineCommand
import com.pixnpunk.logger.AppLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
internal class EngineSwitchTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val root = File(context.cacheDir, "engine-switch")
    private val vfs = FileSystemProviderImpl(context)
    private val errors = CopyOnWriteArrayList<Throwable>()

    private val legacy: NativeEngineBridge by lazy { QspLegacyEngineBridgeImpl(context) }
    private val qsp: NativeEngineBridge by lazy { QspEngineBridgeImpl() }

    private var engine: GameEngineInteractorImpl? = null

    private object NoAudio : AudioPlayerInteractor {
        override var isSoundEnabled = false
        override fun isPlayingFile(path: String) = false
        override fun playFile(path: String, volume: Int) = Unit
        override fun closeFile(path: String) = Unit
        override fun closeAllFiles() = Unit
        override fun pauseAll() = Unit
        override fun resumeAll() = Unit
    }

    private val logger = object : AppLogger {
        override fun recordBreadcrumb(message: String) = Unit
        override fun recordNonFatalException(throwable: Throwable) {
            errors += throwable
        }
    }

    private companion object {
        const val EXT_TEXT = "Ext"
        const val PLAIN_TEXT = "Plain"
    }

    private fun gameWithExtension(dir: File, include: String): File {
        dir.mkdirs()
        File(dir, "ext.qsp").writeBytes(
            TestGame.build(TestGame.Location(name = "extloc", description = EXT_TEXT))
        )
        return File(dir, "main.qsp").apply {
            writeBytes(
                TestGame.build(TestGame.Location(name = "start", code = "$include 'ext.qsp' & gt 'extloc'"))
            )
        }
    }

    private fun plainGame(dir: File): File {
        dir.mkdirs()
        return File(dir, "game.qsp").apply {
            writeBytes(TestGame.build(TestGame.Location(name = "start", description = PLAIN_TEXT)))
        }
    }

    private fun play(step: String, bridge: NativeEngineBridge, game: File, expected: String) = runBlocking {
        engine?.shutdown()
        val current = GameEngineInteractorImpl(lazyOf(bridge), vfs, NoAudio, logger)
        engine = current

        current.submitCommand(EngineCommand.LoadGame(VfsPath(game.absolutePath), VfsPath(game.parent!!)))

        val reached = withTimeoutOrNull(10.seconds) {
            current.state.first {
                (it as? GameState.Running)?.mainDescription?.trim() == expected ||
                    it.activeDialog is EngineDialog.Error
            }
        }
        val state = current.state.value
        val desc = (state as? GameState.Running)?.mainDescription?.trim()
        if (reached == null || desc != expected) {
            fail(
                "Step '$step': expected '$expected', state=${state::class.simpleName}, " +
                    "mainDescription=${desc?.let { "'$it'" }}, dialog=${state.activeDialog}, errors=$errors"
            )
        }
    }

    @Before
    fun setUp() {
        root.deleteRecursively()
    }

    @After
    fun tearDown() {
        engine?.shutdown()
        root.deleteRecursively()
    }

    @Test
    fun legacyGameKeepsItsExtensionAfterAGameOnTheNewEngine() {
        val first = gameWithExtension(File(root, "first"), "addqst")
        val second = plainGame(File(root, "second"))

        play("first game, 5.7.0", legacy, first, EXT_TEXT)
        play("second game, 5.9.5", qsp, second, PLAIN_TEXT)
        play("first game again, 5.7.0", legacy, first, EXT_TEXT)
    }

    @Test
    fun newEngineGameKeepsItsExtensionAfterALegacyGame() {
        val first = gameWithExtension(File(root, "first"), "inclib")
        val second = plainGame(File(root, "second"))

        play("first game, 5.9.5", qsp, first, EXT_TEXT)
        play("second game, 5.7.0", legacy, second, PLAIN_TEXT)
        play("first game again, 5.9.5", qsp, first, EXT_TEXT)
    }

    @Test
    fun legacyGameKeepsItsExtensionWhenRestartedOnTheSameEngine() {
        val first = gameWithExtension(File(root, "first"), "addqst")
        val second = plainGame(File(root, "second"))

        play("first game", legacy, first, EXT_TEXT)
        play("second game", legacy, second, PLAIN_TEXT)
        play("first game again", legacy, first, EXT_TEXT)
    }
}
