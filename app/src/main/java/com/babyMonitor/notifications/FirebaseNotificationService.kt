package com.babyMonitor.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.babyMonitor.MainActivity
import com.babyMonitor.MainApplication
import com.babyMonitor.R
import com.babyMonitor.utils.Utils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseNotificationService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        Log.i(TAG, "New token: $newToken")

        MainApplication.instance.clientTokenRepository.registerNewClientToken(newToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "Message received from: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.i(TAG, "Message data payload: ${remoteMessage.data}")

            when (remoteMessage.data[JSON_TAG_NOTIFICATION_TAG]?.toInt()) {
                NOTIFICATION_TYPE_TEMPERATURE -> parseTemperatureNotification(remoteMessage)
                NOTIFICATION_TYPE_SLEEP_STATE -> parseSleepStateNotification(remoteMessage)
                else -> {
                    Log.w(TAG, "Type of notification not supported")
                }
            }
        }
    }

    private fun parseTemperatureNotification(remoteMessage: RemoteMessage) {
        val temperatureReading: Float = remoteMessage.data[JSON_TAG_TEMPERATURE]?.toFloat() ?: -1f

        when (val typeOfTempWarning: Int =
            remoteMessage.data[JSON_TAG_TYPE_OF_TEMP_WARNING]?.toInt() ?: 0) {
            // High temperature (Fever)
            TYPE_OF_TEMP_WARNING_FEVER -> {
                sendNotification(
                    TEMPERATURE_NOTIFICATION_ID,
                    getString(R.string.notification_title_high_temperature),
                    getString(
                        R.string.notification_message_high_temperature,
                        temperatureReading
                    )
                )
            }

            // Low temperature (Cold)
            TYPE_OF_TEMP_WARNING_COLD -> {
                sendNotification(
                    TEMPERATURE_NOTIFICATION_ID,
                    getString(R.string.notification_title_low_temperature),
                    getString(
                        R.string.notification_message_low_temperature,
                        temperatureReading
                    )
                )
            }

            // Type not supported
            else -> {
                Log.w(TAG, "Type of temperature warning not supported: $typeOfTempWarning")
            }
        }
    }

    private fun parseSleepStateNotification(remoteMessage: RemoteMessage) {
        sendNotification(
            SLEEP_STATE_NOTIFICATION_ID,
            getString(R.string.notification_title_sleep_disturbed),
            getString(
                R.string.notification_message_sleep_disturbed,
                remoteMessage.data[JSON_TAG_BABY_NAME]
            )
        )
    }

    /**
     * Create and show a notification containing the FCM data
     *
     * @param notificationId Identifier of a notification
     * @param messageTitle Title shown in notification
     * @param messageBody Message shown in notification
     */
    private fun sendNotification(notificationId: Int, messageTitle: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        val channelDescription = getString(R.string.default_notification_channel_description)
        val soundUri = Utils.getRawUri(R.raw.baby_notification)
        val vibratePattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setDefaults(Notification.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSmallIcon(R.drawable.ic_baby)
            .setLargeIcon(Utils.getBitmapFromDrawable(this, R.drawable.img_baby_circle))
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(false)
            .setSound(soundUri)
            .setVibrate(vibratePattern)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.setSound(soundUri, audioAttributes)
            channel.lightColor = Color.YELLOW
            channel.enableLights(true)
            channel.description = channelDescription
            channel.vibrationPattern = vibratePattern
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private val TAG: String = FirebaseNotificationService::class.java.simpleName

        private const val NOTIFICATION_TYPE_TEMPERATURE = 1
        private const val NOTIFICATION_TYPE_SLEEP_STATE = 2

        private const val TEMPERATURE_NOTIFICATION_ID = 1
        private const val SLEEP_STATE_NOTIFICATION_ID = 2

        private const val TYPE_OF_TEMP_WARNING_FEVER = 1
        private const val TYPE_OF_TEMP_WARNING_COLD = -1

        private const val JSON_TAG_NOTIFICATION_TAG = "notification_type"
        private const val JSON_TAG_TEMPERATURE = "temperature"
        private const val JSON_TAG_TYPE_OF_TEMP_WARNING = "type_of_temp_warning"
        private const val JSON_TAG_BABY_NAME = "baby_name"
    }
}