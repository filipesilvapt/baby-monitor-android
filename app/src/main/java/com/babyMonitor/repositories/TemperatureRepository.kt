package com.babyMonitor.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babyMonitor.models.ThermometerModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemperatureRepository @Inject constructor(
    private val temperatureHistoryRef: DatabaseReference
) {

    private var lastTemperatureReadingListener: ValueEventListener? = null

    private val _lastTemperatureReading = MutableLiveData<ThermometerModel>().apply {
        value = null
    }
    val lastTemperatureReading: LiveData<ThermometerModel> = _lastTemperatureReading

    /**
     * Listens to changes in the temperature history table of the Firebase realtime database
     * and obtains the most recent value which is posted to a live data object
     */
    fun observeFirebaseLastTemperatureReading() {
        val maxRowsToQuery = 1

        // Read from the database
        lastTemperatureReadingListener = temperatureHistoryRef.limitToLast(maxRowsToQuery)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    // Get the first and only temperature item
                    dataSnapshot.children.firstOrNull()?.getValue(ThermometerModel::class.java)
                        ?.let {
                            Log.d(TAG, "Current temperature reading is: ${it.temp}")

                            _lastTemperatureReading.value = it
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        TAG,
                        "Failed to read firebase baby temperature value.",
                        error.toException()
                    )
                }
            })
    }

    /**
     * Stops listening to the last temperature reading in the Firebase realtime database
     */
    fun stopObservingFirebaseLastTemperatureReading() {
        lastTemperatureReadingListener?.let {
            temperatureHistoryRef.removeEventListener(it)
        }
    }

    companion object {
        private val TAG: String = TemperatureRepository::class.java.simpleName
    }
}