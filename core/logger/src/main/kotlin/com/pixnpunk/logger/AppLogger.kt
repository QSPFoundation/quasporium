package com.pixnpunk.logger

interface AppLogger {
    fun recordBreadcrumb(message: String)
    fun recordNonFatalException(throwable: Throwable)
}
