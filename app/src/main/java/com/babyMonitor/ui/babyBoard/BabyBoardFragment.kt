package com.babyMonitor.ui.babyBoard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.babyMonitor.MainApplication
import com.babyMonitor.databinding.FragmentBabyBoardBinding
import com.babyMonitor.models.TemperatureThresholdsModel
import com.babyMonitor.utils.EventObserver

class BabyBoardFragment : Fragment() {

    private lateinit var viewModel: BabyBoardViewModel

    private val openTemperatureMonitorDirection =
        BabyBoardFragmentDirections.actionOpenTemperatureMonitor()

    private val openSleepMonitorDirection =
        BabyBoardFragmentDirections.actionOpenSleepMonitor()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView")

        viewModel = ViewModelProvider(this).get(BabyBoardViewModel::class.java)
        val binding = FragmentBabyBoardBinding.inflate(layoutInflater, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated - Starting observers")

        // Example #1 of navigation specifically for click actions
        /*image_baby_temperature.setOnClickListener(
            Navigation.createNavigateOnClickListener(
                openTemperatureMonitorDirection
            )
        )*/

        // Example #2 of navigation with direct use of nav controller
        /*image_baby_temperature.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(openTemperatureMonitorDirection)
        }*/

        // Observe temperature click navigation
        viewModel.navigateToTemperatureMonitor.observe(
            viewLifecycleOwner,
            EventObserver { isToNavigate: Boolean ->
                if (isToNavigate) {
                    NavHostFragment.findNavController(this)
                        .navigate(openTemperatureMonitorDirection)
                }
            }
        )

        // Observe sleep state click navigation
        viewModel.navigateToSleepStateMonitor.observe(
            viewLifecycleOwner,
            EventObserver { isToNavigate: Boolean ->
                if (isToNavigate) {
                    NavHostFragment.findNavController(this).navigate(openSleepMonitorDirection)
                }
            }
        )

        // Observe temperature thresholds
        MainApplication.instance.temperatureThresholdsRepository.temperatureThresholds.observe(
            viewLifecycleOwner,
            { thresholds: TemperatureThresholdsModel -> viewModel.updateTemperatureResId(thresholds) }
        )

        // Observe firebase baby name
        viewModel.observeFirebaseBabyName()

        // Observe firebase temperature
        viewModel.observeFirebaseBabyTemperature()

        // Observe firebase sleep state
        viewModel.observeFirebaseBabySleepState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView - Stopping observers")

        MainApplication.instance.temperatureThresholdsRepository.temperatureThresholds.removeObservers(
            viewLifecycleOwner
        )

        viewModel.stopObservingFirebaseBabyName()

        viewModel.stopObservingFirebaseBabyTemperature()

        viewModel.stopObservingFirebaseBabySleepState()
    }

    companion object {
        private val TAG: String = BabyBoardFragment::class.java.simpleName
    }
}