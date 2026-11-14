package com.pixnpunk.stock.data.workers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pixnpunk.stock.domain.repository.RemoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

internal class CancelInstallReceiver : BroadcastReceiver(), KoinComponent {

    companion object {
        private const val EXTRA_FILE_URL = "fileUrl"

        fun pendingIntent(context: Context, fileUrl: String): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                fileUrl.hashCode(),
                Intent(
                    context,
                    CancelInstallReceiver::class.java
                ).putExtra(
                    EXTRA_FILE_URL,
                    fileUrl
                ),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
    }

    override fun onReceive(context: Context, intent: Intent) {
        val fileUrl = intent.getStringExtra(EXTRA_FILE_URL) ?: return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                get<RemoteRepository>().cancelInstall(fileUrl)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
