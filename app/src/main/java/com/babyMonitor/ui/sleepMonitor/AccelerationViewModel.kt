package com.babyMonitor.ui.sleepMonitor

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.models.AccelerometerValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class AccelerationViewModel : ViewModel() {

    private val _accelerometerHistory = MutableLiveData<List<AccelerometerValue>>().apply {
        value = emptyList()
    }
    val accelerometerHistory: LiveData<List<AccelerometerValue>> = _accelerometerHistory

    private lateinit var accelerometerRef: DatabaseReference

    private lateinit var thermometerListener: ValueEventListener

    fun observeFirebaseBabyAccelerometerHistory() {
        val database = Firebase.database
        accelerometerRef = database.getReference(RTDatabasePaths.PATH_ACCELEROMETER_READINGS)

        val rowsToQuery = 100

        // Read from the database
        thermometerListener = accelerometerRef.limitToLast(rowsToQuery)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val listOfTempValues: MutableList<AccelerometerValue> = ArrayList()

                    Log.d(TAG, "-------------------------------------------")
                    for (postSnapshot in dataSnapshot.children) {
                        val value = postSnapshot.getValue(AccelerometerValue::class.java)
                        Log.d(TAG, "Accelerometer read is: $value")

                        value?.let {
                            listOfTempValues.add(it)
                        }
                    }

                    _accelerometerHistory.value = listOfTempValues
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
    }

    fun stopObservingFirebaseBabyAccelerometerHistory() {
        accelerometerRef.removeEventListener(thermometerListener)
    }

    companion object {
        private val TAG: String = AccelerationViewModel::class.java.simpleName
    }
}