package com.pixnpunk.logger

import java.util.IdentityHashMap

class SanitizedException(
    val originalClassName: String,
    cleanedMessage: String?
) : Exception(if (cleanedMessage == null) originalClassName else "$originalClassName: $cleanedMessage")

private val uriRegex = Regex("""\b(content|file)://[^\s"'()<>\[\],]*""")
private val pathRegex = Regex("""(?<![\w/.:])/(storage|sdcard|data|mnt)(/[^\s"'()<>\[\],]*)?""")

fun String.withoutPaths(): String =
    replace(uriRegex) { "${it.groupValues[1]}://…" }
        .replace(pathRegex, "<path>")

fun Throwable.sanitized(): Throwable = sanitize(IdentityHashMap())

private fun Throwable.sanitize(copies: IdentityHashMap<Throwable, Throwable>): Throwable {
    copies[this]?.let { return it }

    val copy = SanitizedException(javaClass.name, message?.withoutPaths())
    copies[this] = copy
    copy.stackTrace = stackTrace

    cause?.let { copy.initCause(it.sanitize(copies)) }
    suppressed.forEach { suppressed ->
        val suppressedCopy = suppressed.sanitize(copies)
        if (suppressedCopy !== copy) copy.addSuppressed(suppressedCopy)
    }
    return copy
}
