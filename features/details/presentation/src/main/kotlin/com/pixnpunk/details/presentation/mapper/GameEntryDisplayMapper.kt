package com.pixnpunk.details.presentation.mapper

import com.pixnpunk.details.presentation.parseCatalogDate
import com.pixnpunk.dto.GameEntry
import org.ocpsoft.prettytime.PrettyTime

internal fun GameEntry.withRelativeDates(prettyTime: PrettyTime): GameEntry =
    copy(
        pubDate = pubDate.toRelativeTime(prettyTime),
        modDate = modDate.toRelativeTime(prettyTime)
    )

private fun String.toRelativeTime(prettyTime: PrettyTime): String =
    parseCatalogDate(this)?.let { prettyTime.format(it) } ?: this
