package com.pixnpunk.details.presentation.mvi

import com.pixnpunk.details.domain.model.SaveItem
import org.junit.Assert.assertEquals
import org.junit.Test

class SavesSyncTest {

    private val dbSaves = listOf(
        SaveItem(id = 1, userTitle = "Before the dragon", filePath = "content://saves/1.sav"),
        SaveItem(id = 2, userTitle = "Ending", filePath = "content://saves/2.sav")
    )

    @Test
    fun incompleteListingChangesNothing() {
        assertEquals(SavesSyncPlan(emptyList(), emptyList()), planSavesSync(null, dbSaves))
    }

    @Test
    fun emptyCompleteListingDeletesAllSaves() {
        assertEquals(SavesSyncPlan(emptyList(), listOf(1L, 2L)), planSavesSync(emptyList(), dbSaves))
    }

    @Test
    fun newFilesAreInsertedAndMissingOnesDeleted() {
        val plan = planSavesSync(
            listOf("content://saves/1.sav", "content://saves/3.sav"),
            dbSaves
        )

        assertEquals(listOf("content://saves/3.sav"), plan.pathsToInsert)
        assertEquals(listOf(2L), plan.idsToDelete)
    }

    @Test
    fun repeatedPathIsInsertedOnce() {
        val plan = planSavesSync(listOf("content://saves/3.sav", "content://saves/3.sav"), emptyList())

        assertEquals(listOf("content://saves/3.sav"), plan.pathsToInsert)
    }
}
