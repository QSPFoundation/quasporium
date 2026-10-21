package com.pixnpunk.game.data.save

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pixnpunk.database.dao.SaveDao
import com.pixnpunk.database.model.SaveEntity
import com.pixnpunk.game.data.TestGame
import com.pixnpunk.game.data.engine.GameEngineInteractorImpl
import com.pixnpunk.game.data.engine.jni.QspLegacyEngineBridgeImpl
import com.pixnpunk.game.data.vfs.impl.FileSystemProviderImpl
import com.pixnpunk.game.domain.model.GameState
import com.pixnpunk.game.domain.model.VfsPath
import com.pixnpunk.game.domain.repository.AudioPlayerInteractor
import com.pixnpunk.game.domain.repository.EngineCommand
import com.pixnpunk.logger.AppLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.milliseconds

@RunWith(AndroidJUnit4::class)
internal class AutosaveTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val errors = CopyOnWriteArrayList<Throwable>()

    private val dir = File(context.cacheDir, "autosave-test")
    private val game = File(dir, "game.qsp")
    private val gamePath = VfsPath(game.absolutePath)

    private lateinit var engine: GameEngineInteractorImpl
    private lateinit var saves: SaveGameInteractorImpl

    private suspend fun awaitMainDescription(step: String, text: String) {
        val reached = withTimeoutOrNull(TIMEOUT_MS.milliseconds) {
            engine.state.first { (it as? GameState.Running)?.mainDescription?.trim() == text }
        }
        if (reached == null) {
            val state = engine.state.value
            val desc = (state as? GameState.Running)?.mainDescription
            fail(
                "Step '$step': expected main description '$text', " +
                        "state=${state::class.simpleName}, mainDescription=${desc?.let { "'$it'" }}, " +
                        "errors=$errors, engineErrors="
            )
        }
    }

    private object NoAudio : AudioPlayerInteractor {
        override var isSoundEnabled = false
        override fun isPlayingFile(path: String) = false
        override fun playFile(path: String, volume: Int) = Unit
        override fun closeFile(path: String) = Unit
        override fun closeAllFiles() = Unit
        override fun pauseAll() = Unit
        override fun resumeAll() = Unit
    }

    private object NoSaves : SaveDao {
        override fun observeSaves(gameId: Long): Flow<List<SaveEntity>> = unused()
        override suspend fun getAllSaves(gameId: Long): List<SaveEntity> = unused()
        override suspend fun getSavesCount(gameId: Long): Int = unused()
        override suspend fun searchByTitle(gameId: Long, title: String): SaveEntity = unused()
        override suspend fun insert(saveSlot: SaveEntity) = unused()
        override suspend fun update(saveSlot: SaveEntity) = unused()
        override suspend fun delete(saveSlot: SaveEntity) = unused()

        private fun unused(): Nothing = error("Autosaves do not use the save slots")
    }

    private companion object {
        const val TIMEOUT_MS = 10_000L
    }

    @Before
    fun setUp() {
        dir.deleteRecursively()
        dir.mkdirs()
        game.writeBytes(TestGame.bytes)

        val logger = object : AppLogger {
            override fun recordBreadcrumb(message: String) = Unit
            override fun recordNonFatalException(throwable: Throwable) {
                errors += throwable
            }
        }
        engine = GameEngineInteractorImpl(
            lazy { QspLegacyEngineBridgeImpl(context) },
            FileSystemProviderImpl(context),
            NoAudio,
            logger
        )
        saves = SaveGameInteractorImpl(context, FileSystemProviderImpl(context), NoSaves, engine)
    }

    @After
    fun tearDown(): Unit = runBlocking {
        engine.shutdown()
        saves.deleteAutosave(gamePath)
        dir.deleteRecursively()
    }

    @Test
    fun restoresTheAutosavedState() = runBlocking {
        engine.submitCommand(EngineCommand.LoadGame(gamePath))
        awaitMainDescription("load", TestGame.START_TEXT)
        engine.submitCommand(EngineCommand.ExecuteAction(0))
        awaitMainDescription("action", TestGame.CLICKED_TEXT)

        saves.autosave(gamePath)
        val autosave = withTimeout(TIMEOUT_MS.milliseconds) {
            var found = saves.findAutosave(gamePath)
            while (found == null) {
                delay(50.milliseconds)
                found = saves.findAutosave(gamePath)
            }
            found
        }

        engine.submitCommand(EngineCommand.RestartGame)
        awaitMainDescription("restart", TestGame.START_TEXT)
        engine.submitCommand(EngineCommand.LoadSave(autosave))
        engine.submitCommand(EngineCommand.ExecuteString(TestGame.CHECK_RESTORED))
        awaitMainDescription("restore", TestGame.RESTORED_TEXT)

        saves.deleteAutosave(gamePath)
        assertNull(saves.findAutosave(gamePath))
        assertEquals(emptyList<Throwable>(), errors)
    }

    @Test
    fun noAutosaveUntilTheGameIsSaved() = runBlocking {
        assertNull(saves.findAutosave(gamePath))
    }
}
