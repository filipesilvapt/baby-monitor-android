package com.babyMonitor.ui.temperatureMonitor

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.models.ThermometerValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class TemperatureMonitorViewModel : ViewModel() {

    private val _temperatureHistory = MutableLiveData<List<ThermometerValue>>().apply {
        value = emptyList()
    }
    val temperatureHistory: LiveData<List<ThermometerValue>> = _temperatureHistory

    private lateinit var thermometerRef: DatabaseReference

    private lateinit var thermometerListener: ValueEventListener

    fun observeFirebaseBabyTemperatureHistory() {
        val database = Firebase.database
        thermometerRef = database.getReference(RTDatabasePaths.PATH_THERMOMETER_READINGS)

        val rowsToQuery = 6

        // Read from the database
        thermometerListener = thermometerRef.limitToLast(rowsToQuery)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val listOfTempValues: MutableList<ThermometerValue> = ArrayList()

                    Log.d(TAG, "-------------------------------------------")
                    for (postSnapshot in dataSnapshot.children) {
                        val value = postSnapshot.getValue(ThermometerValue::class.java)
                        Log.d(TAG, "Thermometer read is: $value")

                        value?.let {
                            listOfTempValues.add(it)
                        }
                    }

                    _temperatureHistory.value = listOfTempValues
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
    }

    fun stopObservingFirebaseBabyTemperatureHistory() {
        thermometerRef.removeEventListener(thermometerListener)
    }

    companion object {
        private val TAG: String = TemperatureMonitorViewModel::class.java.simpleName
    }
}