package com.babyMonitor.ui.babyBoard

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.babyMonitor.MainApplication
import com.babyMonitor.R
import com.babyMonitor.database.RTDatabasePaths
import com.babyMonitor.models.SleepStateModel
import com.babyMonitor.models.TemperatureThresholdsModel
import com.babyMonitor.models.ThermometerModel
import com.babyMonitor.utils.Constants
import com.babyMonitor.utils.FireOnceEvent
import com.babyMonitor.utils.SleepState
import com.babyMonitor.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

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

    private var currentThermometerReading: ThermometerModel? = null

    private val _textBabySleepStateResId = MutableLiveData<Int>().apply {
        value = null
    }
    val textBabySleepStateResId: LiveData<Int> = _textBabySleepStateResId

    private val _isBabyStatusAvailable = MutableLiveData<Boolean>().apply {
        value = false
    }
    val isBabyStatusAvailable: LiveData<Boolean> = _isBabyStatusAvailable

    private val _imageBabySleepStateResId = MutableLiveData<Int>().apply {
        value = null
    }
    val imageBabySleepStateResId: LiveData<Int> = _imageBabySleepStateResId

    private var dataAvailabilityWatcher: Job? = null

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
                Log.w(TAG, "Failed to read firebase baby name value.", error.toException())
            }
        })
    }

    fun stopObservingFirebaseBabyName() {
        babyNameRef.removeEventListener(babyNameListener)
    }

    fun observeFirebaseBabyTemperature() {
        val database = Firebase.database
        babyTemperatureRef = database.getReference(RTDatabasePaths.PATH_THERMOMETER_READINGS)

        val rowsToQuery = 1

        // Read from the database
        babyTemperatureListener = babyTemperatureRef.limitToLast(rowsToQuery)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    // Start the data availability watcher
                    startDataAvailabilityWatcher()

                    // Get the first and only temperature item
                    currentThermometerReading =
                        dataSnapshot.children.firstOrNull()?.getValue(ThermometerModel::class.java)

                    currentThermometerReading?.let {
                        Log.d(TAG, "Current temperature read is: ${it.temp}")

                        // Set the baby status availability
                        setBabyStatusAvailability(it.timestamp)

                        // Set the temperature text
                        _textBabyTemperature.value =
                            "${Utils.getDoubleToStringWithOneDecimal(it.temp)} ºC"

                        // Set the temperature image according to thresholds
                        val thresholds = MainApplication.instance.temperatureThresholds.value
                        thresholds?.let(this@BabyBoardViewModel::updateTemperatureResId)
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
     * Sets the baby status availability according to the last received temperature timestamp.
     * The difference in time should be less than
     */
    fun setBabyStatusAvailability(timestamp: String) {
        // Calculate the difference in milliseconds between the current time and the received timestamp
        val tempTimestampInMillis =
            Utils.convertDateToMillis(timestamp, Utils.FORMAT_DATE_AND_TIME)

        val currentDateTimeInMillis = Utils.getCurrentDateTimeInMillis()

        val timeDifferenceInMillis = currentDateTimeInMillis - tempTimestampInMillis

        Log.d(
            TAG,
            "currentDateTimeInMillis ($currentDateTimeInMillis) - tempTimestampInMillis " +
                    "($tempTimestampInMillis) = $timeDifferenceInMillis || Timestamp = $timestamp"
        )

        // Check if the last value is recent
        if (timeDifferenceInMillis <= Constants.BABY_STATUS_AVAILABILITY_MAX_DELAY) {
            _isBabyStatusAvailable.value = true
        } else {
            // Data availability watcher can be stopped due no recent data
            stopDataAvailabilityWatcher()

            _isBabyStatusAvailable.value = false
        }
    }

    /**
     * Update the temperature resource identifier according to current temperature and the defined
     * temperature thresholds
     */
    fun updateTemperatureResId(thresholds: TemperatureThresholdsModel) {
        currentThermometerReading?.let {
            Log.i(TAG, "Updating temperature res id with received thresholds: $thresholds")
            _imageBabyTemperatureResId.value = when {
                it.temp >= thresholds.highTemp -> R.drawable.ic_temperature_high
                it.temp <= thresholds.lowTemp -> R.drawable.ic_temperature_low
                else -> R.drawable.ic_temperature_normal
            }
        }
    }

    fun stopObservingFirebaseBabyTemperature() {
        babyTemperatureRef.removeEventListener(babyTemperatureListener)

        stopDataAvailabilityWatcher()
    }

    fun observeFirebaseBabySleepState() {
        val database = Firebase.database
        babySleepStateRef = database.getReference(RTDatabasePaths.PATH_SLEEP_STATES)

        val rowsToQuery = 1

        // Read from the database
        babySleepStateListener = babySleepStateRef.limitToLast(rowsToQuery)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.

                    // Get the first and only sleep state item
                    val currentSleepState: SleepStateModel? =
                        dataSnapshot.children.firstOrNull()?.getValue(SleepStateModel::class.java)

                    currentSleepState?.let {
                        Log.d(TAG, "Current sleep state is: $currentSleepState")

                        when (currentSleepState.state) {
                            // Baby is agitated
                            SleepState.AGITATED.value -> {
                                _textBabySleepStateResId.value = R.string.emotion_state_agitated
                                _imageBabySleepStateResId.value =
                                    R.drawable.ic_emotion_state_agitated
                            }

                            // Baby is disturbed
                            SleepState.DISTURBED.value -> {
                                _textBabySleepStateResId.value = R.string.emotion_state_disturbed
                                _imageBabySleepStateResId.value =
                                    R.drawable.ic_emotion_state_disturbed
                            }

                            // Value 0 or default is baby sleeping
                            else -> {
                                _textBabySleepStateResId.value = R.string.emotion_state_sleep
                                _imageBabySleepStateResId.value = R.drawable.ic_emotion_state_sleep
                            }
                        }
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

    fun stopObservingFirebaseBabySleepState() {
        babySleepStateRef.removeEventListener(babySleepStateListener)
    }

    /**
     * Starts a timer to change the availability of the baby status after a specific time without
     * temperature updates
     */
    private fun startDataAvailabilityWatcher() {
        // Stop the previous watcher in case one existed
        stopDataAvailabilityWatcher()

        dataAvailabilityWatcher = GlobalScope.launch {
            delay(Constants.BABY_STATUS_AVAILABILITY_MAX_DELAY)
            withContext(Dispatchers.Main) {
                _isBabyStatusAvailable.value = false
            }
        }
    }

    /**
     * Stops the watcher for cases like:
     * - New temperature data received
     * - Moved out of the current screen
     */
    private fun stopDataAvailabilityWatcher() {
        dataAvailabilityWatcher?.cancel()
        dataAvailabilityWatcher = null
    }

    companion object {
        private val TAG: String = BabyBoardViewModel::class.java.simpleName
    }

}