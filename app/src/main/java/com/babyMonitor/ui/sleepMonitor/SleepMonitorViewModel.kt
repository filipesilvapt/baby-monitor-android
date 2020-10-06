package com.babyMonitor.ui.sleepMonitor

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.models.SleepStateValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SleepMonitorViewModel : ViewModel() {

    private val _sleepStatesHistory = MutableLiveData<List<SleepStateValue>>().apply {
        value = emptyList()
    }
    val sleepStatesHistory: LiveData<List<SleepStateValue>> = _sleepStatesHistory

    private lateinit var sleepStatesRef: DatabaseReference

    private lateinit var sleepStatesListener: ValueEventListener

    fun observeFirebaseSleepStatesHistory() {
        val database = Firebase.database
        sleepStatesRef = database.getReference(RTDatabasePaths.PATH_SLEEP_STATES)

        val rowsToQuery = 100

        // Read from the database
        sleepStatesListener = sleepStatesRef.limitToLast(rowsToQuery)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val listOfSleepStates: MutableList<SleepStateValue> = ArrayList()

                    Log.d(TAG, "-------------------------------------------")
                    for (postSnapshot in dataSnapshot.children) {
                        val value = postSnapshot.getValue(SleepStateValue::class.java)
                        Log.d(TAG, "Sleep state is: $value")

                        value?.let {
                            listOfSleepStates.add(it)
                        }
                    }

                    _sleepStatesHistory.value = listOfSleepStates
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read firebase sleep states history.", error.toException())
                }
            })
    }

    fun stopObservingFirebaseSleepStatesHistory() {
        sleepStatesRef.removeEventListener(sleepStatesListener)
    }

    companion object {
        private val TAG: String = SleepMonitorViewModel::class.java.simpleName
    }
}