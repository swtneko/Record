package com.neko.record.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.neko.record.MainActivity
import com.neko.record.R

/**
 * Builds the persistent notification required to keep [ScreenCaptureService]
 * alive as a foreground service (spec section 13: "Livestream Notification —
 * Foreground Service, Persistent Notification, Không bị Android kill").
 */
object ScreenCaptureNotifications {

    const val CHANNEL_ID = "screen_capture_channel"
    const val NOTIFICATION_ID = 1001
    const val ACTION_STOP = "com.neko.record.action.STOP_CAPTURE"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_capture),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_capture_description)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(context: Context, contentText: String): android.app.Notification {
        ensureChannel(context)

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            context,
            0,
            Intent(context, ScreenCaptureService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_capture_title))
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(0, context.getString(R.string.notification_action_stop), stopIntent)
            .build()
    }
}
