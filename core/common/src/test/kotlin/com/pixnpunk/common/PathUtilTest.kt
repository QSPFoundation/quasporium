package com.pixnpunk.common

import com.pixnpunk.common.PathUtil.toRootRelativePath
import com.pixnpunk.common.PathUtil.toSafeRelativePath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PathUtilTest {

    @Test
    fun keepsPathsInsideRoot() {
        assertEquals("images/pic.png", "images/pic.png".toSafeRelativePath())
        assertEquals("images/pic.png", "./images/pic.png".toSafeRelativePath())
        assertEquals("images/pic.png", "images\\pic.png".toSafeRelativePath())
        assertEquals("images/pic.png", "/images//./pic.png".toSafeRelativePath())
        assertEquals("pic.png", "images/../pic.png".toSafeRelativePath())
        assertEquals("C:/game/pic.png", "C:\\game\\pic.png".toSafeRelativePath())
    }

    @Test
    fun rejectsPathsEscapingRoot() {
        assertNull("../../shared_prefs/prefs.xml".toSafeRelativePath())
        assertNull("..\\..\\databases\\games.db".toSafeRelativePath())
        assertNull("images/../../games.db".toSafeRelativePath())
        assertNull("/../games.db".toSafeRelativePath())
    }

    @Test
    fun rejectsEmptyPaths() {
        assertNull("".toSafeRelativePath())
        assertNull("./".toSafeRelativePath())
        assertNull("images/..".toSafeRelativePath())
    }

    @Test
    fun keepsAbsolutePathsInsideRoot() {
        val root = "/storage/emulated/0/Games/game"
        assertEquals("pic.png", "/storage/emulated/0/Games/game/pic.png".toRootRelativePath(root))
        assertEquals("img/pic.png", "/storage/emulated/0/Games/game/img/pic.png".toRootRelativePath(root))
        assertEquals("img/pic.png", "./img\\pic.png".toRootRelativePath(root))
    }

    @Test
    fun rejectsAbsolutePathsOutsideRoot() {
        val root = "/storage/emulated/0/Games/game"
        assertNull("/data/user/0/com.pixnpunk.quasporium/databases/games.db".toRootRelativePath(root))
        assertNull("/storage/emulated/0/Games/game2/pic.png".toRootRelativePath(root))
        assertNull("/storage/emulated/0/Games/game/../other/pic.png".toRootRelativePath(root))
        assertNull("/storage/emulated/0/Games/game/pic.png".toRootRelativePath(""))
        assertNull("../shared_prefs/prefs.xml".toRootRelativePath(root))
    }
}
