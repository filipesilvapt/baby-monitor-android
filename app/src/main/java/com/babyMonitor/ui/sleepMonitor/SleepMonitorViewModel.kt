package com.babyMonitor.ui.sleepMonitor

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.babyMonitor.models.SleepStateModel
import com.babyMonitor.repositories.SleepStateRepository

class SleepMonitorViewModel @ViewModelInject constructor(
    private val sleepStateRepository: SleepStateRepository
) : ViewModel() {

    private val _sleepStatesHistory = MutableLiveData<List<SleepStateModel>>().apply {
        value = emptyList()
    }
    val sleepStatesHistory: LiveData<List<SleepStateModel>> = _sleepStatesHistory

    /**
     * Update the history list whenever a new sleep state is added
     */
    fun observeSleepStateHistory() {
        val rowsToQuery = 100

        sleepStateRepository.observeFirebaseSleepStateHistory(
            rowsToQuery,
            _sleepStatesHistory
        )
    }

    /**
     * Stop observing changes to the sleep state history
     */
    fun stopObservingSleepStateHistory() {
        sleepStateRepository.stopObservingFirebaseSleepStateHistory()
    }
}