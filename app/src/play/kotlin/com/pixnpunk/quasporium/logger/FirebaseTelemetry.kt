package com.pixnpunk.quasporium.logger

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.ConsentStatus
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.pixnpunk.logger.GameStartEvent
import com.pixnpunk.logger.Telemetry

internal class FirebaseTelemetry(private val context: Context) : Telemetry {

    private val analytics by lazy { FirebaseAnalytics.getInstance(context) }

    private companion object {
        const val MAX_PARAM_LENGTH = 100
    }

    override val isAvailable = true

    override fun setUsageStatisticsEnabled(enabled: Boolean) {
        analytics.setConsent(
            mapOf(
                ConsentType.ANALYTICS_STORAGE to if (enabled) ConsentStatus.GRANTED else ConsentStatus.DENIED,
                ConsentType.AD_STORAGE to ConsentStatus.DENIED,
                ConsentType.AD_USER_DATA to ConsentStatus.DENIED,
                ConsentType.AD_PERSONALIZATION to ConsentStatus.DENIED
            )
        )
        analytics.setAnalyticsCollectionEnabled(enabled)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = enabled
    }

    override fun setCrashReportsEnabled(enabled: Boolean) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
    }

    override fun logGameStarted(event: GameStartEvent) {
        val params = Bundle().apply {
            putString("engine", event.engine)
            putString("source", if (event.isCatalogGame) "catalog" else "local")
            putString("mode", if (event.isContinue) "continue" else "new")
            if (event.isCatalogGame) putString("game_title", event.title.take(MAX_PARAM_LENGTH))
        }
        analytics.logEvent("game_start", params)
    }
}
