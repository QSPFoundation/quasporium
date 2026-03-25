package com.pixnpunk.logger

interface AppLogger {
    fun recordNonFatalException(throwable: Throwable)
}