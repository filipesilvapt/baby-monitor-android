package com.babyMonitor.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.babyMonitor.BuildConfig
import com.babyMonitor.MainActivity
import com.babyMonitor.R
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.utils.Utils
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseNotificationService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        Log.i(TAG, "New token: $newToken")

        sendRegistrationToServer(newToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "Message received from: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.i(TAG, "Message data payload: ${remoteMessage.data}")
            val temperatureReading: Float = remoteMessage.data["temperature"]?.toFloat() ?: -1f

            when (val typeOfTempWarning: Int =
                remoteMessage.data["typeOfTempWarning"]?.toInt() ?: 0) {
                // High temperature (Fever)
                1 -> {
                    sendNotification(
                        getString(R.string.notification_title_high_temperature),
                        getString(
                            R.string.notification_message_high_temperature,
                            temperatureReading
                        )
                    )
                }

                // Low temperature (Cold)
                -1 -> {
                    sendNotification(
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
    }

    /**
     * Create and show a notification containing the FCM data
     *
     * @param messageTitle Title shown in notification
     * @param messageBody Message shown in notification
     */
    private fun sendNotification(messageTitle: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = getString(R.string.default_notification_channel_name)
        val channelDescription = getString(R.string.default_notification_channel_description)
        val soundUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                    + BuildConfig.APPLICATION_ID + "/" + R.raw.baby_notification
        )
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

        notificationManager.notify(0, notificationBuilder.build())
    }

    /**
     * Register the Firebase device token in the RT Database associated with a unique device id
     */
    private fun sendRegistrationToServer(newToken: String) {
        val database = Firebase.database
        val clientTokensRef = database.getReference(RTDatabasePaths.PATH_CLIENT_TOKENS)
        val uniqueId = Utils.getAndroidDeviceId(this)
        val map = HashMap<String, Any>()
        map[uniqueId] = newToken
        clientTokensRef.updateChildren(map)
            .addOnSuccessListener {
                Log.i(TAG, "Firebase token was successfully saved in RT Database with id $uniqueId")
            }
            .addOnFailureListener {
                Log.w(TAG, "Firebase token failed to be saved in RT Database")
            }
    }

    companion object {
        private val TAG: String = FirebaseNotificationService::class.java.simpleName
    }
}