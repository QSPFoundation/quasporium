package com.pixnpunk.game.data.engine

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pixnpunk.game.data.TestGame
import com.pixnpunk.game.data.engine.jni.NativeEngineBridge
import com.pixnpunk.game.data.engine.jni.NativeEngineListener
import com.pixnpunk.game.data.engine.jni.QspEngineBridgeImpl
import com.pixnpunk.game.data.engine.jni.QspLegacyEngineBridgeImpl
import com.pixnpunk.game.data.model.RawItem
import com.pixnpunk.game.data.vfs.VirtualFile
import com.pixnpunk.game.data.vfs.impl.DirectVirtualFile
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.model.WindowType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Loads a tiny game into a real engine library, plays it, saves and restores the state. */
internal abstract class NativeEngineSmokeTest {
    protected val context: Context = ApplicationProvider.getApplicationContext()
    protected val listener = RecordingListener()

    private lateinit var bridge: NativeEngineBridge
    private lateinit var dir: File

    abstract fun createBridge(): NativeEngineBridge

    @Before
    fun setUp() {
        dir = File(context.cacheDir, "engine-smoke").apply {
            deleteRecursively()
            mkdirs()
        }
        bridge = createBridge()
        bridge.setListener(listener)
        bridge.initializeEngine()
    }

    @After
    fun tearDown() {
        bridge.destroyEngine()
        dir.deleteRecursively()
    }

    @Test
    fun playsSavesAndRestoresAGame() = runBlocking {
        val game = File(dir, "game.qsp").apply { writeBytes(TestGame.bytes) }

        assertTrue(listener.failure(), bridge.loadGame(DirectVirtualFile(game), isGame = true))
        assertTrue(listener.failure(), bridge.restartGame())
        assertEquals(TestGame.START_TEXT, listener.mainDesc.trim())
        assertEquals(listOf("Click"), listener.actions.map { it.text })

        assertTrue(listener.failure(), bridge.executeAction(0))
        assertEquals(TestGame.CLICKED_TEXT, listener.mainDesc.trim())

        val save = File(dir, "game.sav").apply { createNewFile() }
        assertTrue(listener.failure(), bridge.saveGameState(DirectVirtualFile(save)))
        assertTrue(save.length() > 0)

        assertTrue(listener.failure(), bridge.restartGame())
        assertEquals(TestGame.START_TEXT, listener.mainDesc.trim())

        assertTrue(listener.failure(), bridge.loadGameState(DirectVirtualFile(save)))
        assertTrue(listener.failure(), bridge.executeString(TestGame.CHECK_RESTORED))
        assertEquals(TestGame.RESTORED_TEXT, listener.mainDesc.trim())
        assertEquals(emptyList<String>(), listener.errors)
    }

    @Test
    fun rejectsAFileThatIsNotAGame() = runBlocking {
        val game = File(dir, "broken.qsp").apply { writeBytes(byteArrayOf(1, 2, 3)) }

        assertEquals(false, bridge.loadGame(DirectVirtualFile(game), isGame = true))
    }

    protected suspend fun loadFromMemory(data: ByteArray): Boolean =
        bridge.loadGame(InMemoryFile(data), isGame = true)

    protected fun restart(): Boolean = bridge.restartGame()

    private class InMemoryFile(private var data: ByteArray) : VirtualFile by unsupported() {
        override suspend fun readBytes() = Result.success(data)
        override suspend fun writeBytes(data: ByteArray): Result<Unit> {
            this.data = data
            return Result.success(Unit)
        }
    }

    protected class RecordingListener : NativeEngineListener {
        var mainDesc = ""
        var actions: List<RawItem> = emptyList()
        val errors = mutableListOf<String>()

        fun failure() = "Engine errors: $errors"

        override fun onStateRefreshed(
            mainDesc: String,
            varsDesc: String,
            uiConfig: LibUIConfig,
            actions: List<RawItem>,
            objects: List<RawItem>
        ) {
            this.mainDesc = mainDesc
            this.actions = actions
        }

        override fun onError(details: String) {
            errors += details
        }

        override fun onSetTimer(msecs: Int) = Unit
        override fun onShowMessage(text: String) = Unit
        override fun onShowImage(path: String) = Unit
        override fun onInputBox(prompt: String) = ""
        override fun onShowMenu(items: List<RawItem>) = -1
        override fun onShowWindow(type: WindowType, isVisible: Boolean) = Unit
        override fun onOpenGame(path: String, isGame: Boolean) = Unit
        override fun onSaveGame(path: String) = Unit
        override fun onLoadSave(path: String) = Unit
        override fun onCheckAudioPlaying(path: String) = false
        override fun onPlayAudio(path: String, volume: Int) = Unit
        override fun onCloseAudio(path: String?) = Unit
    }

    private companion object {
        fun unsupported(): VirtualFile =
            java.lang.reflect.Proxy.newProxyInstance(
                VirtualFile::class.java.classLoader,
                arrayOf(VirtualFile::class.java)
            ) { _, method, _ -> throw UnsupportedOperationException(method.name) } as VirtualFile
    }
}

@RunWith(AndroidJUnit4::class)
internal class QspEngineSmokeTest : NativeEngineSmokeTest() {
    override fun createBridge(): NativeEngineBridge = QspEngineBridgeImpl()
}

@RunWith(AndroidJUnit4::class)
internal class QspLegacyEngineSmokeTest : NativeEngineSmokeTest() {
    override fun createBridge(): NativeEngineBridge = QspLegacyEngineBridgeImpl(context)

    @Test
    fun loadsAGameFromMemory() = runBlocking {
        assertTrue(listener.failure(), loadFromMemory(TestGame.bytes))
        assertTrue(listener.failure(), restart())
        assertEquals(TestGame.START_TEXT, listener.mainDesc.trim())
    }
}
