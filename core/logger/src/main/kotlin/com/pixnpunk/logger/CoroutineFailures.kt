package com.pixnpunk.logger

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

fun AppLogger.coroutineExceptionHandler(scopeName: String): CoroutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        recordBreadcrumb("Unhandled coroutine exception in $scopeName")
        recordNonFatalException(throwable)
    }

fun AppLogger.executorContext(executorName: String): CoroutineContext =
    Dispatchers.Main + SupervisorJob() + coroutineExceptionHandler(executorName)
