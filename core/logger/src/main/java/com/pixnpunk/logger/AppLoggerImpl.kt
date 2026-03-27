package com.pixnpunk.logger

import com.google.firebase.crashlytics.FirebaseCrashlytics

class AppLoggerImpl : AppLogger {
    override fun recordNonFatalException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}