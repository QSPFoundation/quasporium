package com.pixnpunk.details.presentation.mvi

import com.pixnpunk.details.domain.model.SaveItem

internal data class SavesSyncPlan(
    val pathsToInsert: List<String>,
    val idsToDelete: List<Long>
)

internal fun planSavesSync(diskPaths: List<String>?, dbSaves: List<SaveItem>): SavesSyncPlan {
    if (diskPaths == null) return SavesSyncPlan(emptyList(), emptyList())

    val diskPathsSet = diskPaths.toSet()
    val dbPathsSet = dbSaves.map { it.filePath }.toSet()

    return SavesSyncPlan(
        pathsToInsert = diskPathsSet.filterNot { it in dbPathsSet },
        idsToDelete = dbSaves.filterNot { it.filePath in diskPathsSet }.map { it.id }
    )
}
