package com.pixnpunk.quasporium.logger

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pixnpunk.logger.AppLogger

internal class FirebaseAppLogger : AppLogger {

    override fun recordBreadcrumb(message: String) {
        FirebaseCrashlytics.getInstance().log(message)
    }

    override fun recordNonFatalException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}
