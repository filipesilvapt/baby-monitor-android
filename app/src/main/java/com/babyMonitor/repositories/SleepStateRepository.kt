package com.babyMonitor.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.babyMonitor.models.SleepStateModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepStateRepository @Inject constructor(
    private val sleepStateHistoryRef: DatabaseReference
) {

    private var lastSleepStateListener: ValueEventListener? = null

    private var sleepStateHistoryListener: ValueEventListener? = null

    private val _lastSleepStateResult = MutableLiveData<Int>().apply {
        value = -1
    }
    val lastSleepStateResult: LiveData<Int> = _lastSleepStateResult

    /**
     * Listens to changes in the sleep state history table of the Firebase realtime database
     * and obtains the most recent value which is posted to a live data object
     */
    fun observeFirebaseLastSleepState() {
        val maxRowsToQuery = 1

        // Read from the database
        lastSleepStateListener = sleepStateHistoryRef.limitToLast(maxRowsToQuery)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    // Get the first and only sleep state item
                    val currentSleepState: SleepStateModel? =
                        dataSnapshot.children.firstOrNull()?.getValue(SleepStateModel::class.java)

                    currentSleepState?.let {
                        Log.d(TAG, "Current sleep state is: $currentSleepState")

                        _lastSleepStateResult.value = currentSleepState.state
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(
                        TAG,
                        "Failed to read firebase baby sleep state value.",
                        error.toException()
                    )
                }
            })
    }

    /**
     * Stops listening to the last sleep state in the Firebase realtime database
     */
    fun stopObservingFirebaseLastSleepState() {
        lastSleepStateListener?.let {
            sleepStateHistoryRef.removeEventListener(it)
        }
    }

    /**
     * Listens to changes in the sleep state history table of the Firebase realtime database
     * and translates it to a list that is posted to the received live data object
     */
    fun observeFirebaseSleepStateHistory(
        maxRowsToQuery: Int,
        sleepStateListToUpdate: MutableLiveData<List<SleepStateModel>>
    ) {
        sleepStateHistoryListener = sleepStateHistoryRef.limitToLast(maxRowsToQuery)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val listOfSleepStates: MutableList<SleepStateModel> = ArrayList()

                    Log.d(TAG, "-------------------------------------------")
                    for (postSnapshot in dataSnapshot.children) {
                        val value = postSnapshot.getValue(SleepStateModel::class.java)
                        Log.d(TAG, "Sleep state is: $value")

                        value?.let {
                            listOfSleepStates.add(it)
                        }
                    }

                    sleepStateListToUpdate.value = listOfSleepStates
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read firebase sleep states history.", error.toException())
                }
            })
    }

    /**
     * Stops listening to the sleep state history table in the Firebase realtime database
     */
    fun stopObservingFirebaseSleepStateHistory() {
        sleepStateHistoryListener?.let {
            sleepStateHistoryRef.removeEventListener(it)
        }
    }

    companion object {
        private val TAG: String = SleepStateRepository::class.java.simpleName
    }
}