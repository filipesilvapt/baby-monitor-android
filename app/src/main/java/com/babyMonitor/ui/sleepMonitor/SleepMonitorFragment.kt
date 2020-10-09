package com.babyMonitor.ui.sleepMonitor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.babyMonitor.R
import com.babyMonitor.models.SleepStateModel

class SleepMonitorFragment : Fragment() {

    private lateinit var sleepMonitorViewModel: SleepMonitorViewModel

    private lateinit var listSleepStates: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView")

        sleepMonitorViewModel =
            ViewModelProvider(this).get(SleepMonitorViewModel::class.java)
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

        sleepMonitorViewModel.observeFirebaseSleepStatesHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView - Stopping observers")
        sleepMonitorViewModel.sleepStatesHistory.removeObservers(viewLifecycleOwner)
        sleepMonitorViewModel.stopObservingFirebaseSleepStatesHistory()
    }

    companion object {
        private val TAG: String = SleepMonitorFragment::class.java.simpleName
    }

}