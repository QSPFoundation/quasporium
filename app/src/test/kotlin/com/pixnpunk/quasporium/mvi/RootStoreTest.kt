package com.pixnpunk.quasporium.mvi

import com.arkivanov.mvikotlin.core.utils.isAssertOnMainThreadEnabled
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.pixnpunk.dto.GameEntry
import com.pixnpunk.dto.InputState
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.settings.domain.repository.SettingsRepository
import com.pixnpunk.settings.domain.usecase.RestoreColorValueUseCase
import com.pixnpunk.settings.domain.usecase.UpdateColorUseCase
import com.pixnpunk.settings.domain.usecase.UpdateCustomFontPathUseCase
import com.pixnpunk.settings.domain.usecase.UpdateImageSizeUseCase
import com.pixnpunk.stock.domain.repository.LocalRepository
import com.pixnpunk.stock.domain.repository.RemoteRepository
import com.pixnpunk.stock.domain.usecase.DownloadGameArchUseCase
import com.pixnpunk.stock.domain.usecase.FillAndInsertEntryUseCase
import com.pixnpunk.stock.domain.usecase.MigrateIfNeededUseCase
import com.pixnpunk.stock.domain.usecase.UpsertEntryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.lang.reflect.Proxy

@OptIn(ExperimentalCoroutinesApi::class)
class RootStoreTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val calls = mutableListOf<String>()
    private var migrationError: Exception? = null

    private val localRepository = object : LocalRepository by unimplemented() {
        override suspend fun migrateIfNeeded() {
            calls += "migrate"
            migrationError?.let { throw it }
        }

        override suspend fun fillAndInsertEntry(entry: GameEntry) {
            calls += "insert ${entry.title}"
        }

        override suspend fun upsertEntry(entry: GameEntry) {
            calls += "upsert ${entry.title}"
        }
    }

    private val remoteRepository = object : RemoteRepository by unimplemented() {
        override suspend fun downloadGame(downloadEntry: GameEntry) {
            calls += "download ${downloadEntry.title}"
        }
    }

    private val settingsRepository = object : SettingsRepository by unimplemented() {
        override suspend fun updateImageSize(state: InputState, value: Int) {
            calls += "image size $value"
        }

        override suspend fun updateCustomFontPath(filePath: String) {
            calls += "font $filePath"
        }
    }

    private val logger = object : AppLogger {
        override fun recordBreadcrumb(message: String) = Unit
        override fun recordNonFatalException(throwable: Throwable) = Unit
    }

    @Before
    fun setUp() {
        isAssertOnMainThreadEnabled = false
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        isAssertOnMainThreadEnabled = true
    }

    private fun createStore(): RootStore =
        RealRootStore(DefaultStoreFactory()) {
            RootExecutor(
                fillAndInsertEntryUseCase = FillAndInsertEntryUseCase(localRepository),
                upsertEntryUseCase = UpsertEntryUseCase(localRepository),
                downloadGameArchUseCase = DownloadGameArchUseCase(remoteRepository),
                updateColorUseCase = UpdateColorUseCase(settingsRepository),
                restoreColorValueUseCase = RestoreColorValueUseCase(settingsRepository),
                updateImageSizeUseCase = UpdateImageSizeUseCase(settingsRepository),
                updateCustomFontPathUseCase = UpdateCustomFontPathUseCase(settingsRepository),
                migrateIfNeededUseCase = MigrateIfNeededUseCase(localRepository),
                logger = logger,
                mainContext = dispatcher,
                ioContext = dispatcher
            )
        }.create()

    @Test
    fun `migration runs when the store is created`() {
        val store = createStore()

        assertEquals(listOf("migrate"), calls)
        assertTrue(store.state.isMigrationComplete)
    }

    @Test
    fun `migration failure still completes startup`() {
        migrationError = IllegalStateException("broken database")

        val store = createStore()

        assertTrue(store.state.isMigrationComplete)
    }

    @Test
    fun `game intents reach the repositories`() {
        val store = createStore()
        calls.clear()

        store.accept(RootStore.Intent.AddNewGame(GameEntry(title = "a")))
        store.accept(RootStore.Intent.EditGame(GameEntry(title = "b")))
        store.accept(RootStore.Intent.DownloadNewGame(GameEntry(title = "c")))

        assertEquals(listOf("insert a", "upsert b", "download c"), calls)
    }

    @Test
    fun `settings intents reach the settings repository`() {
        val store = createStore()
        calls.clear()

        store.accept(RootStore.Intent.UpdateImageConfig(InputState.entries.first(), "42"))
        store.accept(RootStore.Intent.UpdateCustomFontPath("content://font"))

        assertEquals(listOf("image size 42", "font content://font"), calls)
    }

    @Test
    fun `replaced custom font file is deleted after the new one is saved`() {
        val oldFont = File.createTempFile("custom_font", ".ttf")
        val store = createStore()
        calls.clear()

        store.accept(RootStore.Intent.UpdateCustomFontPath("/fonts/new.ttf", replacedPath = oldFont.path))

        assertEquals(listOf("font /fonts/new.ttf"), calls)
        assertFalse(oldFont.exists())
    }
}

/** A stand-in for an interface whose members the test does not expect to be called. */
private inline fun <reified T : Any> unimplemented(): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
        throw UnsupportedOperationException("${T::class.simpleName}.${method.name} is not expected in this test")
    } as T
