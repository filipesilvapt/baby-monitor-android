package com.babyMonitor.repositories

import android.content.Context
import android.util.Log
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.utils.Utils
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientTokenRepository @Inject constructor(
    private val applicationContext: Context,
    private val database: FirebaseDatabase
) {

    /**
     * Register the Firebase device token in the RT Database associated with a unique device id
     */
    fun registerNewClientToken(newToken: String) {
        val clientTokensRef = database.getReference(RTDatabasePaths.PATH_CLIENT_TOKENS)
        val uniqueId = Utils.getAndroidDeviceId(applicationContext)
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
        private val TAG: String = ClientTokenRepository::class.java.simpleName
    }
}