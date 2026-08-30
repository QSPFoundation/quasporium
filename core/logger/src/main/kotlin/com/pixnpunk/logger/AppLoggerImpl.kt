package com.pixnpunk.logger

import com.google.firebase.crashlytics.FirebaseCrashlytics

class AppLoggerImpl : AppLogger {

    override fun recordBreadcrumb(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    override fun recordNonFatalException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}
