package com.babyMonitor.ui.sleepMonitor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.babyMonitor.R
import com.babyMonitor.models.SleepStateModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SleepMonitorFragment : Fragment() {

    private val sleepMonitorViewModel: SleepMonitorViewModel by viewModels()

    private lateinit var listSleepStates: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView")

        val root = inflater.inflate(R.layout.fragment_sleep_monitor, container, false)

        listSleepStates = root.findViewById(R.id.list_sleep_states)

        listSleepStates.layoutManager = LinearLayoutManager(requireContext())

        listSleepStates.adapter = SleepStateAdapter()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated - Starting observers")

        sleepMonitorViewModel.sleepStatesHistory.observe(
            viewLifecycleOwner,
            { newSleepStates: List<SleepStateModel>? ->
                (listSleepStates.adapter as SleepStateAdapter).updateItems(newSleepStates)
            })

        sleepMonitorViewModel.observeSleepStateHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView - Stopping observers")
        sleepMonitorViewModel.sleepStatesHistory.removeObservers(viewLifecycleOwner)
        sleepMonitorViewModel.stopObservingSleepStateHistory()
    }

    companion object {
        private val TAG: String = SleepMonitorFragment::class.java.simpleName
    }

}