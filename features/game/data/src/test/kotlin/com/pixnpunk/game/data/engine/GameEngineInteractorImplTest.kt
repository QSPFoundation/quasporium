package com.pixnpunk.game.data.engine

import com.pixnpunk.game.data.engine.jni.NativeEngineBridge
import com.pixnpunk.game.data.engine.jni.NativeEngineListener
import com.pixnpunk.game.data.vfs.FileSystemProvider
import com.pixnpunk.game.data.vfs.VirtualFile
import com.pixnpunk.game.data.vfs.VirtualFileStream
import com.pixnpunk.game.domain.model.EngineDialog
import com.pixnpunk.game.domain.model.LibUIConfig
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.game.domain.repository.AudioPlayerInteractor
import com.pixnpunk.game.domain.repository.EngineCommand
import com.pixnpunk.logger.AppLogger
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class GameEngineInteractorImplTest {

    private class FakeBridge(val log: MutableList<String>) : NativeEngineBridge {
        lateinit var engineListener: NativeEngineListener
        val counterTicks = AtomicInteger()
        val release = CountDownLatch(1)

        override fun initializeEngine() {
            log += "init"
        }

        override fun destroyEngine() {
            log += "destroy@" + Thread.currentThread().name.substringBefore('-')
        }

        override fun executeAction(index: Int): Boolean {
            log += "action$index"
            when (index) {
                ACTION_INPUT -> log += "input=" + engineListener.onInputBox("Your name?")
                ACTION_MENU -> log += "menu=" + engineListener.onShowMenu(emptyList())
                ACTION_LONG_SCRIPT -> {
                    Thread.sleep(500)
                    log += "longScriptDone"
                }
                ACTION_TWO_INPUTS -> {
                    log += "input=" + engineListener.onInputBox("1")
                    log += "input2=" + engineListener.onInputBox("2")
                }
                ACTION_FAILING -> throw IllegalStateException("native call failed")
                ACTION_BLOCKING -> release.await(3, TimeUnit.SECONDS)
            }
            return true
        }

        override fun executeCounter(): Boolean {
            counterTicks.incrementAndGet()
            return true
        }

        override fun executeString(input: String): Boolean {
            log += "exec:$input"
            return true
        }

        override suspend fun loadGame(file: VirtualFile, isGame: Boolean): Boolean {
            log += "loadGame"
            return true
        }

        override suspend fun saveGameState(file: VirtualFile): Boolean {
            log += "saveGame"
            return true
        }

        override suspend fun loadGameState(file: VirtualFile): Boolean {
            log += "loadSave"
            return true
        }

        override fun restartGame(): Boolean {
            log += "restart"
            return true
        }

        override fun selectObject(index: Int) = true
        override fun onInputAreaClicked(text: String) {}

        override fun setListener(listener: NativeEngineListener) {
            engineListener = listener
        }
    }

    private object NoFiles : FileSystemProvider {
        override fun setGameRoot(rootPath: VfsPath) {}
        override fun resolve(path: VfsPath): VirtualFile? = null
        override fun resolveRelative(path: String): VirtualFile? = null
    }

    private object AnyFiles : FileSystemProvider {
        override fun setGameRoot(rootPath: VfsPath) {}
        override fun resolve(path: VfsPath): VirtualFile = FakeFile(path)
        override fun resolveRelative(path: String): VirtualFile = FakeFile(VfsPath(path))
    }

    private class FakeFile(override val uriPath: VfsPath) : VirtualFile {
        override val absPath = uriPath
        override val name = uriPath.value.substringAfterLast('/')
        override val isDirectory = false
        override val isWritable = true
        override suspend fun readBytes() = Result.success(ByteArray(0))
        override suspend fun writeBytes(data: ByteArray) = Result.success(Unit)
        override fun getChild(name: String): VirtualFile? = null
        override fun getParent(): VirtualFile? = null
        override fun makeDir(name: String): VirtualFile? = null
        override fun makeFile(name: String, mimeType: String): VirtualFile? = null
        override fun delete() = false
        override fun openStream(): Result<VirtualFileStream> = Result.failure(UnsupportedOperationException())
    }

    private class FakeAudio(val log: MutableList<String>) : AudioPlayerInteractor {
        override var isSoundEnabled = false
        override fun isPlayingFile(path: String) = false
        override fun playFile(path: String, volume: Int) {}
        override fun closeFile(path: String) {}
        override fun closeAllFiles() {
            log += "audioClosed"
        }

        override fun pauseAll() {}
        override fun resumeAll() {}
    }

    private val log = CopyOnWriteArrayList<String>()
    private val bridge = FakeBridge(log)
    private val reported = CopyOnWriteArrayList<Throwable>()
    private val logger = object : AppLogger {
        override fun recordBreadcrumb(message: String) {}
        override fun recordNonFatalException(throwable: Throwable) {
            reported += throwable
        }
    }
    private val engines = mutableListOf<GameEngineInteractorImpl>()

    private fun newEngine(files: FileSystemProvider = NoFiles) =
        GameEngineInteractorImpl(lazyOf(bridge), files, FakeAudio(log), logger).also { engines += it }

    private fun waitUntil(timeoutMs: Long = 3000, condition: () -> Boolean): Boolean {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            if (condition()) return true
            Thread.sleep(10)
        }
        return condition()
    }

    private fun callsAfterBusyEngine(vararg commands: EngineCommand, lastCall: String): List<String> {
        val engine = newEngine(AnyFiles)
        engine.submitCommand(EngineCommand.ExecuteAction(ACTION_BLOCKING))
        assertTrue(waitUntil { "action$ACTION_BLOCKING" in log })

        commands.forEach(engine::submitCommand)
        bridge.release.countDown()

        assertTrue("$log", waitUntil { lastCall in log })
        return log.dropWhile { it != "action$ACTION_BLOCKING" }.drop(1).filter { it != "audioClosed" }
    }

    private companion object {
        const val ACTION_INPUT = 0
        const val ACTION_SIMPLE = 1
        const val ACTION_MENU = 2
        const val ACTION_LONG_SCRIPT = 3
        const val ACTION_TWO_INPUTS = 4
        const val ACTION_FAILING = 5
        const val ACTION_BLOCKING = 6
    }

    @After
    fun shutdownEngines() {
        // The engine thread is shared by all instances: leave it free for the next test
        engines.forEach { it.shutdown() }
        waitUntil { log.count { it.startsWith("destroy") } >= log.count { it == "init" } }
    }

    @Test
    fun dismissingInputReturnsEmptyStringAndReleasesEngine() {
        val engine = newEngine()
        engine.submitCommand(EngineCommand.ExecuteAction(ACTION_INPUT))
        assertTrue(waitUntil { engine.state.value.activeDialog is EngineDialog.Input })

        engine.submitCommand(EngineCommand.DismissDialog)
        engine.submitCommand(EngineCommand.ExecuteAction(ACTION_SIMPLE))

        assertTrue("engine continues after dismiss: $log", waitUntil { "action$ACTION_SIMPLE" in log })
        assertEquals(listOf("init", "action0", "input=", "action1"), log.toList())
    }

    @Test
    fun dismissingMenuReturnsMinusOne() {
        val engine = newEngine()
        engine.submitCommand(EngineCommand.ExecuteAction(ACTION_MENU))
        assertTrue(waitUntil { engine.state.value.activeDialog is EngineDialog.Menu })

        engine.submitCommand(EngineCommand.DismissDialog)

        assertTrue("$log", waitUntil { "menu=-1" in log })
    }

    @Test
    fun failedCommandIsReportedAndLoopContinues() {
        val engine = newEngine()
        engine.submitCommand(EngineCommand.ExecuteAction(ACTION_FAILING))
        assertTrue("error shown: $log", waitUntil { engine.state.value.activeDialog is EngineDialog.Error })
        assertEquals("native call failed", reported.single().message)

        engine.submitCommand(EngineCommand.DismissDialog)
        engine.submitCommand(EngineCommand.ExecuteAction(ACTION_SIMPLE))

        assertTrue("engine loop still runs: $log", waitUntil { "action$ACTION_SIMPLE" in log })
    }

    @Test
    fun timerDoesNotTickWhilePaused() {
        val engine = newEngine()
        engine.onSetTimer(20)
        engine.onStateRefreshed("", "", LibUIConfig(), emptyList(), emptyList())
        assertTrue("timer runs", waitUntil { bridge.counterTicks.get() >= 3 })

        engine.setTimerPaused(true)
        Thread.sleep(60)
        val ticksWhenPaused = bridge.counterTicks.get()
        Thread.sleep(300)
        assertEquals(ticksWhenPaused, bridge.counterTicks.get())

        engine.setTimerPaused(false)
        assertTrue("timer resumes", waitUntil { bridge.counterTicks.get() >= ticksWhenPaused + 3 })
    }

    @Test
    fun shutdownWithOpenDialogDestroysEngineBeforeNextInit() {
        val first = newEngine()
        first.submitCommand(EngineCommand.ExecuteAction(ACTION_TWO_INPUTS))
        assertTrue(waitUntil { first.state.value.activeDialog is EngineDialog.Input })

        first.shutdown()
        first.shutdown() // idempotent
        newEngine()

        assertTrue("$log", waitUntil { log.count { it == "init" } == 2 })
        // After shutdown the script's second INPUT returns at once instead of blocking the engine thread
        assertEquals(
            listOf("init", "action4", "input=", "input2=", "destroy@DefaultDispatcher", "init"),
            log.filter { it != "audioClosed" }
        )
        assertTrue("audioClosed" in log)
    }

    @Test
    fun shutdownDuringLongScriptWaitsForIt() {
        val first = newEngine()
        first.submitCommand(EngineCommand.ExecuteAction(ACTION_LONG_SCRIPT))
        assertTrue(waitUntil { "action$ACTION_LONG_SCRIPT" in log })

        first.shutdown()
        newEngine()

        assertTrue("$log", waitUntil { log.count { it == "init" } == 2 })
        val order = log.toList()
        val destroyIndex = order.indexOfFirst { it.startsWith("destroy") }
        assertTrue("$order", order.indexOf("longScriptDone") < destroyIndex)
        assertTrue("$order", destroyIndex < order.lastIndexOf("init"))
    }

    @Test
    fun loadedSaveIsInPlaceForTheNextCommand() {
        val calls = callsAfterBusyEngine(
            EngineCommand.LoadSave(VfsPath("/game.sav")),
            EngineCommand.ExecuteString("check"),
            lastCall = "exec:check"
        )

        assertEquals(listOf("loadSave", "exec:check"), calls)
    }

    @Test
    fun loadedGameIsStartedBeforeTheNextCommand() {
        val calls = callsAfterBusyEngine(
            EngineCommand.LoadGame(VfsPath("/game.qsp")),
            EngineCommand.ExecuteAction(ACTION_SIMPLE),
            lastCall = "action$ACTION_SIMPLE"
        )

        assertEquals(listOf("loadGame", "restart", "action$ACTION_SIMPLE"), calls)
    }

    @Test
    fun gameIsSavedBeforeTheNextCommand() {
        val calls = callsAfterBusyEngine(
            EngineCommand.SaveGame(VfsPath("/game.sav")),
            EngineCommand.ExecuteAction(ACTION_SIMPLE),
            lastCall = "action$ACTION_SIMPLE"
        )

        assertEquals(listOf("saveGame", "action$ACTION_SIMPLE"), calls)
    }

    @Test
    fun gameIsRestartedBeforeTheNextCommand() {
        val calls = callsAfterBusyEngine(
            EngineCommand.RestartGame,
            EngineCommand.ExecuteString("check"),
            lastCall = "exec:check"
        )

        assertEquals(listOf("restart", "exec:check"), calls)
    }
}
