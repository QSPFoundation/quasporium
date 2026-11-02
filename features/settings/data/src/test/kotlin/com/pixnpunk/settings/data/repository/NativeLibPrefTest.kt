package com.pixnpunk.settings.data.repository

import com.pixnpunk.dto.NativeLib
import org.junit.Assert.assertEquals
import org.junit.Test

class NativeLibPrefTest {

    @Test
    fun readsCurrentNames() {
        assertEquals(NativeLib.QSP, nativeLibFromPref("QSP", NativeLib.QSPLEGACY))
        assertEquals(NativeLib.QSPLEGACY, nativeLibFromPref("QSPLEGACY", NativeLib.QSP))
    }

    @Test
    fun readsNamesStoredBeforeTheRename() {
        assertEquals(NativeLib.QSP, nativeLibFromPref("BYTE", NativeLib.QSPLEGACY))
        assertEquals(NativeLib.QSPLEGACY, nativeLibFromPref("SEEDHARTHA", NativeLib.QSP))
    }

    @Test
    fun fallsBackToTheDefaultForUnknownValues() {
        assertEquals(NativeLib.QSPLEGACY, nativeLibFromPref("", NativeLib.QSPLEGACY))
        assertEquals(NativeLib.QSP, nativeLibFromPref("qsp", NativeLib.QSP))
    }
}
