package com.babyMonitor

import android.app.Application
import android.util.Log
import com.babyMonitor.repositories.ClientTokenRepository
import com.babyMonitor.repositories.TemperatureThresholdsRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainApplication : Application() {

    lateinit var clientTokenRepository: ClientTokenRepository
        private set

    lateinit var temperatureThresholdsRepository: TemperatureThresholdsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")

        instance = this

        clientTokenRepository = ClientTokenRepository(instance, Firebase.database)

        temperatureThresholdsRepository = TemperatureThresholdsRepository(Firebase.database)

        registerDeviceInFirebaseDatabase()

        temperatureThresholdsRepository.observeFirebaseTemperatureThresholds()
    }

    /**
     * Register the device Firebase token in the database every time the app opens.
     * This will ensure that if a wipe occurs in the database side, the app will still make sure
     * to register its token to be able to receive notifications.
     */
    private fun registerDeviceInFirebaseDatabase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token and save it
            task.result?.let {
                Log.i(TAG, "Fetched FCM registration token: $it")
                clientTokenRepository.registerNewClientToken(it)
            }
        })
    }

    companion object {
        private val TAG: String = MainApplication::class.java.simpleName

        lateinit var instance: MainApplication
            private set
    }
}