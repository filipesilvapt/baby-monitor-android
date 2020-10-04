package com.babyMonitor.ui.sleepMonitor

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.models.SleepDeviationValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SleepMonitorViewModel : ViewModel() {

    private val _sleepDeviationsHistory = MutableLiveData<List<SleepDeviationValue>>().apply {
        value = emptyList()
    }
    val sleepDeviationsHistory: LiveData<List<SleepDeviationValue>> = _sleepDeviationsHistory

    private lateinit var sleepDeviationsRef: DatabaseReference

    private lateinit var sleepDeviationsListener: ValueEventListener

    fun observeFirebaseSleepDeviationsHistory() {
        val database = Firebase.database
        sleepDeviationsRef = database.getReference(RTDatabasePaths.PATH_SLEEP_DEVIATIONS)

        val rowsToQuery = 100

        // Read from the database
        sleepDeviationsListener = sleepDeviationsRef.limitToLast(rowsToQuery)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val listOfTempValues: MutableList<SleepDeviationValue> = ArrayList()

                    Log.d(TAG, "-------------------------------------------")
                    for (postSnapshot in dataSnapshot.children) {
                        val value = postSnapshot.getValue(SleepDeviationValue::class.java)
                        Log.d(TAG, "Sleep deviation is: $value")

                        value?.let {
                            listOfTempValues.add(it)
                        }
                    }

                    _sleepDeviationsHistory.value = listOfTempValues
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
    }

    fun stopObservingFirebaseSleepDeviationsHistory() {
        sleepDeviationsRef.removeEventListener(sleepDeviationsListener)
    }

    companion object {
        private val TAG: String = SleepMonitorViewModel::class.java.simpleName
    }
}