package com.pixnpunk.quasporium.logger

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pixnpunk.logger.AppLogger
import com.pixnpunk.logger.sanitized
import com.pixnpunk.logger.withoutPaths

internal class FirebaseAppLogger : AppLogger {

    override fun recordBreadcrumb(message: String) {
        FirebaseCrashlytics.getInstance().log(message.withoutPaths())
    }

    override fun recordNonFatalException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable.sanitized())
    }
}
