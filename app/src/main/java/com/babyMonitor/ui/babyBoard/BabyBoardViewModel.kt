package com.babyMonitor.ui.babyBoard

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.babyMonitor.R
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.utils.FireOnceEvent
import com.babyMonitor.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class BabyBoardViewModel : ViewModel() {

    private val _textBabyName = MutableLiveData<String>().apply {
        value = ""
    }
    val textBabyName: LiveData<String> = _textBabyName

    private lateinit var babyNameRef: DatabaseReference

    private lateinit var babyNameListener: ValueEventListener

    private val _textBabyTemperature = MutableLiveData<String>().apply {
        value = ""
    }
    val textBabyTemperature: LiveData<String> = _textBabyTemperature

    private val _imageBabyTemperatureResId = MutableLiveData<Int>().apply {
        value = null
    }
    val imageBabyTemperatureResId: LiveData<Int> = _imageBabyTemperatureResId

    private lateinit var babyTemperatureRef: DatabaseReference

    private lateinit var babyTemperatureListener: ValueEventListener

    private val _textBabySleepStateResId = MutableLiveData<Int>().apply {
        value = null
    }
    val textBabySleepStateResId: LiveData<Int> = _textBabySleepStateResId

    private val _imageBabySleepStateResId = MutableLiveData<Int>().apply {
        value = null
    }
    val imageBabySleepStateResId: LiveData<Int> = _imageBabySleepStateResId

    private lateinit var babySleepStateRef: DatabaseReference

    private lateinit var babySleepStateListener: ValueEventListener

    private val _navigateToTemperatureMonitor = MutableLiveData<FireOnceEvent<Boolean>>()
    val navigateToTemperatureMonitor: LiveData<FireOnceEvent<Boolean>> =
        _navigateToTemperatureMonitor

    private val _navigateToSleepStateMonitor = MutableLiveData<FireOnceEvent<Boolean>>()
    val navigateToSleepStateMonitor: LiveData<FireOnceEvent<Boolean>> = _navigateToSleepStateMonitor

    fun onButtonPressedTemperature(@Suppress("UNUSED_PARAMETER") view: View) {
        Log.i(TAG, "Button pressed - temperature")
        _navigateToTemperatureMonitor.value = FireOnceEvent(true)
    }

    fun onButtonPressedSleepState(@Suppress("UNUSED_PARAMETER") view: View) {
        Log.i(TAG, "Button pressed - sleep state")
        _navigateToSleepStateMonitor.value = FireOnceEvent(true)
    }

    fun observeFirebaseBabyName() {
        val database = Firebase.database
        babyNameRef = database.getReference(RTDatabasePaths.PATH_BABY_NAME)

        // Read from the database
        babyNameListener = babyNameRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                Log.i(TAG, "Baby name is: $value")

                value?.let {
                    _textBabyName.value = it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    fun stopObservingFirebaseBabyName() {
        babyNameRef.removeEventListener(babyNameListener)
    }

    fun observeFirebaseBabyTemperature() {
        val database = Firebase.database
        babyTemperatureRef = database.getReference(RTDatabasePaths.PATH_LAST_THERMOMETER_READING)


        // Read from the database
        babyTemperatureListener =
            babyTemperatureRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val currentTemp = dataSnapshot.getValue(Double::class.java) ?: 0.0

                    Log.d(TAG, "Current temperature read is: $currentTemp")

                    // Set the temperature text
                    _textBabyTemperature.value = "${Utils.getDoubleOneDecimal(currentTemp)} ºC"

                    // Set the temperature image
                    _imageBabyTemperatureResId.value = when {
                        currentTemp > 38.0 -> R.drawable.ic_temperature_high
                        currentTemp < 36.0 -> R.drawable.ic_temperature_low
                        else -> R.drawable.ic_temperature_normal
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
    }

    fun stopObservingFirebaseBabyTemperature() {
        babyTemperatureRef.removeEventListener(babyTemperatureListener)
    }

    fun observeFirebaseBabySleepState() {
        val database = Firebase.database
        babySleepStateRef = database.getReference(RTDatabasePaths.PATH_LAST_SLEEP_STATE)

        // Read from the database
        babySleepStateListener =
            babySleepStateRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val currentSleepState = dataSnapshot.getValue(Int::class.java)
                    Log.d(TAG, "Current sleep state is: $currentSleepState")

                    when (currentSleepState) {
                        // Baby is agitated
                        1 -> {
                            _textBabySleepStateResId.value = R.string.emotion_state_agitated
                            _imageBabySleepStateResId.value = R.drawable.ic_emotion_state_agitated
                        }

                        // Baby is disturbed
                        2 -> {
                            _textBabySleepStateResId.value = R.string.emotion_state_disturbed
                            _imageBabySleepStateResId.value = R.drawable.ic_emotion_state_disturbed
                        }

                        // Value 0 or default is baby sleeping
                        else -> {
                            _textBabySleepStateResId.value = R.string.emotion_state_sleep
                            _imageBabySleepStateResId.value = R.drawable.ic_emotion_state_sleep
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
    }

    fun stopObservingFirebaseBabySleepState() {
        babySleepStateRef.removeEventListener(babySleepStateListener)
    }

    companion object {
        private val TAG: String = BabyBoardViewModel::class.java.simpleName
    }

}