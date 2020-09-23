package com.babyMonitor

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        /*FirebaseMessaging.getInstance().subscribeToTopic("temperatureTopic")
            .addOnCompleteListener { task ->
                var msg = "Subscribed to topic successfully"
                if (!task.isSuccessful) {
                    msg = "Subscribed to topic failed"
                }
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }*/
    }

    companion object {
        private val TAG: String = MainApplication::class.java.simpleName
    }
}