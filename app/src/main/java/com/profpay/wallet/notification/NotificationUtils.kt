package com.profpay.wallet.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.profpay.wallet.MainActivity
import com.profpay.wallet.R
import me.pushy.sdk.Pushy
import kotlin.random.Random

/**
 * Утилита для отображения push-уведомлений.
 */
object NotificationUtils {

    private const val CHANNEL_ID = "PUSHY_SERVICE_CHANNEL"
    private const val CHANNEL_NAME = "Pushy Notifications"

    /**
     * Показывает push-уведомление.
     *
     * @param context контекст приложения
     * @param title заголовок уведомления
     * @param text текст уведомления
     * @param icon иконка уведомления (по умолчанию app icon)
     */
    fun showNotification(
        context: Context,
        title: String,
        text: String,
        @DrawableRes icon: Int = R.drawable.icon_smart,
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        ensureChannelExists(notificationManager)

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setAutoCancel(true)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setLights(Color.RED, 1000, 1000)
            .setVibrate(VIBRATION_PATTERN)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .build()

        Pushy.setNotificationChannel(
            NotificationCompat.Builder(context, CHANNEL_ID),
            context,
        )

        notificationManager.notify(Random.nextInt(100_000), notification)
    }

    private fun ensureChannelExists(manager: NotificationManager) {
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            enableLights(true)
            lightColor = Color.RED
            enableVibration(true)
            vibrationPattern = VIBRATION_PATTERN
        }

        manager.createNotificationChannel(channel)
    }

    private val VIBRATION_PATTERN = longArrayOf(0, 400, 250, 400)
}
