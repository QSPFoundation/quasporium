package com.pixnpunk.details.presentation.mvi

import com.arkivanov.mvikotlin.core.rx.observer
import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.details.domain.model.PresetItem
import com.pixnpunk.details.domain.model.SaveItem
import com.pixnpunk.details.domain.repository.PresetRepository
import com.pixnpunk.details.domain.repository.SavesRepository
import com.pixnpunk.details.presentation.model.GameFileItem
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.GameListType
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.settings.domain.repository.GameSettingsPreloader
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.stock.domain.usecase.ObserveEntryByIdUseCase
import com.pixnpunk.storage.GameFileStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsStoreTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val entry = GameEntry(
        id = 7,
        listId = GameListType.Playable,
        title = "Game",
        gameDirPath = "content://games/game"
    )

    private val entryFlow = MutableStateFlow<GameEntry?>(entry)
    private val presets = MutableStateFlow(
        listOf(
            PresetItem(
                id = 1, name = "First", timestamp = 0, lastPlayedFile = "content://games/game/main.qsp",
                accentColor = 0, backImagePath = ""
            ),
            PresetItem(
                id = 2, name = "Second", timestamp = 0, lastPlayedFile = null,
                accentColor = 0xFF123456, backImagePath = ""
            )
        )
    )
    private val dbSaves = mutableListOf(
        SaveItem(id = 10, gameId = 7, userTitle = "kept", filePath = "content://games/game/kept.sav"),
        SaveItem(id = 11, gameId = 7, userTitle = "gone", filePath = "content://games/game/gone.sav")
    )
    private val savesFlow = MutableStateFlow(dbSaves.toList())

    private val calls = mutableListOf<String>()

    private val localRepository = object : LocalRepository by unimplemented() {
        override fun observeEntryById(entryId: Long): Flow<GameEntry?> = entryFlow
    }

    private val presetRepository = object : PresetRepository by unimplemented() {
        override fun observeAllPresets(gameId: Long) = presets

        override suspend fun createPreset(gameId: Long, presetName: String, filePath: String?): Long {
            calls += "create $gameId $presetName"
            return 3
        }

        override suspend fun updateTimestamp(presetId: Long) {
            calls += "timestamp $presetId"
        }

        override suspend fun updateLastPlayedFile(presetId: Long, fileName: String) {
            calls += "last played $presetId $fileName"
        }
    }

    private val savesRepository = object : SavesRepository by unimplemented() {
        override fun observeAllSaves(gameId: Long) = savesFlow

        override suspend fun getAllSaves(gameId: Long) = dbSaves.toList()

        override suspend fun insertSave(gameId: Long, userTitle: String, filePath: String) {
            calls += "insert save $userTitle"
        }

        override suspend fun deleteSave(saveId: Long) {
            calls += "delete save $saveId"
        }
    }

    private val gameFileStorage = object : GameFileStorage {
        override suspend fun receiveGameFiles(rootDirPath: String) =
            listOf("$rootDirPath/main.qsp", "$rootDirPath/extra.qsp")

        override suspend fun receiveGameSaves(rootDirPath: String): List<String> =
            listOf("$rootDirPath/kept.sav", "$rootDirPath/new.sav")

        override fun receiveFullName(path: String) = path.substringAfterLast('/')

        override fun receiveBaseName(path: String) = path.substringAfterLast('/').substringBeforeLast('.')
    }

    private val gameSettingsPreloader = object : GameSettingsPreloader {
        override suspend fun preloadGameSettings(gameId: Long) {
            calls += "preload settings $gameId"
        }
    }

    private val logger = object : AppLogger {
        override fun recordBreadcrumb(message: String) = Unit
        override fun recordNonFatalException(throwable: Throwable) = Unit
    }

    private val labels = mutableListOf<DetailsStore.Label>()
    private lateinit var store: DetailsStore

    @Before
    fun setUp() {
        isAssertOnMainThreadEnabled = false
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        if (::store.isInitialized) store.dispose()
        Dispatchers.resetMain()
        isAssertOnMainThreadEnabled = true
    }

    private fun createStore(gameEntry: GameEntry = entry): DetailsStore {
        store = RealDetailsStore(DefaultStoreFactory(), gameEntry) {
            DetailsExecutor(
                logger = logger,
                observeEntryByIdUseCase = ObserveEntryByIdUseCase(localRepository),
                presetRepository = presetRepository,
                savesRepository = savesRepository,
                gameFileStorage = gameFileStorage,
                gameSettingsPreloader = gameSettingsPreloader,
                mainContext = dispatcher,
                ioContext = dispatcher
            )
        }.create()
        store.labels(observer { labels += it })
        return store
    }

    @Test
    fun `playable game loads entry, profiles and files`() {
        val state = createStore().state

        assertFalse(state.isLoading)
        assertEquals(entry, state.gameEntry)
        assertEquals(
            listOf("main.qsp", "extra.qsp"),
            state.gameFiles.map { it.displayName }
        )

        val (first, second) = state.gameProfiles
        assertEquals(GameFileItem("main.qsp", "content://games/game/main.qsp"), first.lastPlayedFile)
        assertEquals(0xFF3B9E9BL, first.accentColor) // default colour picked by preset id
        assertEquals(0xFF123456, second.accentColor)
        assertEquals(listOf("kept", "gone"), first.saves.map { it.displayName })
    }

    @Test
    fun `saves on disk are synchronised with the database`() {
        createStore()

        assertTrue("insert save new" in calls)
        assertTrue("delete save 11" in calls)
        assertFalse(calls.any { it.startsWith("insert save kept") })
    }

    @Test
    fun `game settings are preloaded for a playable game`() {
        createStore()

        assertTrue("preload settings 7" in calls)
    }

    @Test
    fun `non-playable game is not observed`() {
        val state = createStore(entry.copy(listId = GameListType.Downloadable)).state

        assertTrue(state.isLoading)
        assertTrue(state.gameFiles.isEmpty())
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `deleted entry publishes a label`() {
        createStore()

        entryFlow.value = null

        assertEquals(listOf(DetailsStore.Label.NullUpdateReceived), labels)
    }

    @Test
    fun `new profile is numbered after the existing ones`() {
        createStore().accept(DetailsStore.Intent.CreateProfile)

        assertEquals("create 7 Playthrough 3", calls.last())
    }

    @Test
    fun `continue uses the last played file of the profile`() {
        createStore().accept(DetailsStore.Intent.ContinueGame(presetId = 1, savePath = "s.sav"))

        assertEquals("timestamp 1", calls.last())
        assertEquals(
            DetailsStore.Label.LaunchEngine(1, "content://games/game/main.qsp", "s.sav"),
            labels.last()
        )
    }

    @Test
    fun `continue falls back to the first game file`() {
        createStore().accept(DetailsStore.Intent.ContinueGame(presetId = 2, savePath = "s.sav"))

        assertEquals(
            DetailsStore.Label.LaunchEngine(2, "content://games/game/main.qsp", "s.sav"),
            labels.last()
        )
    }

    @Test
    fun `start fresh remembers the file before launching`() {
        createStore().accept(DetailsStore.Intent.StartFresh(presetId = 2, fileName = "extra.qsp"))

        assertEquals(listOf("last played 2 extra.qsp", "timestamp 2"), calls.takeLast(2))
        assertEquals(DetailsStore.Label.LaunchEngine(2, "extra.qsp"), labels.last())
    }
}

/** A stand-in for an interface whose members the test does not expect to be called. */
private inline fun <reified T : Any> unimplemented(): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
        throw UnsupportedOperationException("${T::class.simpleName}.${method.name} is not expected in this test")
    } as T
