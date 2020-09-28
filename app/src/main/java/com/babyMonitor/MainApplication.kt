package com.babyMonitor

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.models.TemperatureThresholds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainApplication : Application() {

    private lateinit var temperatureThresholdsRef: DatabaseReference

    private val _temperatureThresholds = MutableLiveData<TemperatureThresholds>().apply {
        value = TemperatureThresholds(
            HIGH_TEMPERATURE_DEFAULT_THRESHOLD,
            LOW_TEMPERATURE_DEFAULT_THRESHOLD
        )
    }
    val temperatureThresholds: LiveData<TemperatureThresholds> = _temperatureThresholds

    override fun onCreate() {
        super.onCreate()

        instance = this

        observeFirebaseTemperatureThresholds()
    }

    /**
     * Observe the temperature threshold values during the entire application life
     */
    private fun observeFirebaseTemperatureThresholds() {
        val database = Firebase.database
        temperatureThresholdsRef =
            database.getReference(RTDatabasePaths.PATH_TEMPERATURE_THRESHOLDS)

        // Read from the database
        temperatureThresholdsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(TemperatureThresholds::class.java)
                Log.i(TAG, "Temperature thresholds are: $value")

                value?.let {
                    _temperatureThresholds.value = it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    companion object {
        private val TAG: String = MainApplication::class.java.simpleName

        lateinit var instance: MainApplication
            private set

        private const val HIGH_TEMPERATURE_DEFAULT_THRESHOLD: Double = 37.5
        private const val LOW_TEMPERATURE_DEFAULT_THRESHOLD: Double = 36.0
    }
}