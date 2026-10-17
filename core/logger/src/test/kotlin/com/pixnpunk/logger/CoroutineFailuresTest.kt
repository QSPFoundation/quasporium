package com.pixnpunk.logger

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineFailuresTest {

    private val breadcrumbs = mutableListOf<String>()
    private val reported = mutableListOf<Throwable>()
    private val logger = object : AppLogger {
        override fun recordBreadcrumb(message: String) {
            breadcrumbs += message
        }

        override fun recordNonFatalException(throwable: Throwable) {
            reported += throwable
        }
    }

    @Before
    fun setMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun failedLaunchIsReportedAndScopeKeepsWorking() {
        val scope = CoroutineScope(logger.executorContext("TestExecutor"))
        var isSecondLaunchRun = false

        scope.launch { error("intent failed") }
        scope.launch { isSecondLaunchRun = true }

        assertTrue(scope.isActive)
        assertTrue(isSecondLaunchRun)
        assertEquals("intent failed", reported.single().message)
        assertEquals(listOf("Unhandled coroutine exception in TestExecutor"), breadcrumbs)
    }
}
