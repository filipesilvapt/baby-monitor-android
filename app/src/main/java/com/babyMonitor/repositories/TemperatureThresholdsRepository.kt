package com.babyMonitor.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.models.TemperatureThresholdsModel
import com.babyMonitor.utils.Constants
import com.google.firebase.database.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemperatureThresholdsRepository @Inject constructor(
    private val database: FirebaseDatabase
) {

    private lateinit var temperatureThresholdsRef: DatabaseReference

    private val _temperatureThresholds = MutableLiveData<TemperatureThresholdsModel>().apply {
        value = TemperatureThresholdsModel(
            Constants.HIGH_TEMPERATURE_DEFAULT_THRESHOLD,
            Constants.LOW_TEMPERATURE_DEFAULT_THRESHOLD
        )
    }
    val temperatureThresholds: LiveData<TemperatureThresholdsModel> = _temperatureThresholds

    /**
     * Observe the temperature threshold values during the entire application life
     */
    fun observeFirebaseTemperatureThresholds() {
        temperatureThresholdsRef =
            database.getReference(RTDatabasePaths.PATH_TEMPERATURE_THRESHOLDS)

        // Read from the database
        temperatureThresholdsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(TemperatureThresholdsModel::class.java)
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
        private val TAG: String = TemperatureThresholdsRepository::class.java.simpleName
    }
}