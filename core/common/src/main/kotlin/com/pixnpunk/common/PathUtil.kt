package com.pixnpunk.common

import java.io.File

object PathUtil {
    fun String.getFilename(): String {
        val idx = this.lastIndexOf('/')
        return if (idx == -1) this else this.substring(idx + 1)
    }

    fun String.toRootRelativePath(rootDirAbsPath: String): String? =
        if (!this.startsWith('/')) {
            toSafeRelativePath()
        } else if (rootDirAbsPath.isBlank()) {
            null
        } else {
            File(this).toRelativeString(File(rootDirAbsPath)).toSafeRelativePath()
        }

    /**
     * Leads to the normal form of the path to the game resource (melodies, images).
     *
     * @implNote Removes "./" from the beginning of the path, replaces all occurrences of "\" with "/".
     */
    fun String.normalizeContentPath(): String {
        var result: String = this
        if (result.startsWith("./")) {
            result = result.substring(2)
        }
        return result.replace("\\", "/")
    }

    fun String.toSafeRelativePath(): String? {
        val segments = ArrayDeque<String>()
        for (segment in replace('\\', '/').split('/')) {
            when (segment) {
                "", "." -> continue
                ".." -> segments.removeLastOrNull() ?: return null
                else -> segments.addLast(segment)
            }
        }
        return if (segments.isEmpty()) null else segments.joinToString("/")
    }
}
