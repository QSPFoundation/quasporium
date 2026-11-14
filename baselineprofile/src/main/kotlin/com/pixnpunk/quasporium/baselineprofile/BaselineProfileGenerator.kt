package com.pixnpunk.quasporium.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    private companion object {
        const val PACKAGE_NAME = "com.pixnpunk.quasporium"
        const val UI_TIMEOUT_MS = 5_000L
        const val CATALOG_TIMEOUT_MS = 20_000L
    }

    @get:Rule
    val rule = BaselineProfileRule()

    private fun MacrobenchmarkScope.openDetails(game: UiObject2) {
        game.click()
        device.wait(Until.hasObject(By.text("Description")), UI_TIMEOUT_MS)
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.browse(catalog: UiObject2) {
        device.waitForIdle()
        catalog.fling(Direction.DOWN)
        device.waitForIdle()
        catalog.fling(Direction.UP)
        device.waitForIdle()
        catalog.children.firstOrNull()?.let { openDetails(it) }
    }

    private fun MacrobenchmarkScope.openCatalog() {
        device.wait(Until.findObject(By.text("Library")), UI_TIMEOUT_MS)
            ?.also { it.click() }
            ?.let { device.wait(Until.findObject(By.scrollable(true)), CATALOG_TIMEOUT_MS) }
            ?.let { browse(it) }
    }

    @Test
    fun startupAndCatalog() =
        rule.collect(packageName = PACKAGE_NAME, includeInStartupProfile = true) {
            device.executeShellCommand("pm grant $packageName android.permission.POST_NOTIFICATIONS")
            pressHome()
            startActivityAndWait()
            openCatalog()
        }
}
