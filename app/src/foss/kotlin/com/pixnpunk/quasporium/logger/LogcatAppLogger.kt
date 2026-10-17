package com.pixnpunk.quasporium.logger

import android.util.Log
import com.pixnpunk.logger.AppLogger

internal class LogcatAppLogger : AppLogger {

    override fun recordBreadcrumb(message: String) {
        Log.i(TAG, message)
    }

    override fun recordNonFatalException(throwable: Throwable) {
        Log.e(TAG, "Non-fatal error", throwable)
    }

    private companion object {
        const val TAG = "Quasporium"
    }
}
