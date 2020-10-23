package com.babyMonitor.ui.babyBoard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.babyMonitor.databinding.FragmentBabyBoardBinding
import com.babyMonitor.models.TemperatureThresholdsModel
import com.babyMonitor.models.ThermometerModel
import com.babyMonitor.repositories.TemperatureThresholdsRepository
import com.babyMonitor.utils.EventObserver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BabyBoardFragment : Fragment() {

    @Inject
    lateinit var temperatureThresholdsRepository: TemperatureThresholdsRepository

    private val viewModel: BabyBoardViewModel by viewModels()

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
        temperatureThresholdsRepository.temperatureThresholds.observe(
            viewLifecycleOwner,
            { thresholds: TemperatureThresholdsModel ->
                viewModel.updateTemperatureImageResId(
                    thresholds
                )
            }
        )

        // Observe firebase baby name
        viewModel.observeFirebaseBabyName()

        // Observe firebase temperature
        viewModel.getLastTemperatureReading().observe(
            viewLifecycleOwner,
            { temperatureReading: ThermometerModel? ->
                viewModel.run {
                    startDataAvailabilityWatcher()
                    setBabyStatusAvailability(temperatureReading?.timestamp)
                    temperatureReading?.let { updateTemperatureResources(it) }
                }
            }
        )
        viewModel.startObservingLastTemperatureReading()

        // Observe firebase sleep state
        viewModel.getLastSleepStateResult().observe(
            viewLifecycleOwner,
            { sleepState: Int -> viewModel.updateSleepStateResources(sleepState) }
        )
        viewModel.startObservingLastSleepState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView - Stopping observers")

        temperatureThresholdsRepository.temperatureThresholds.removeObservers(
            viewLifecycleOwner
        )

        viewModel.stopObservingFirebaseBabyName()

        // Stop observing firebase temperature
        viewModel.run {
            stopDataAvailabilityWatcher()
            getLastTemperatureReading().removeObservers(
                viewLifecycleOwner
            )
            stopObservingLastTemperatureReading()
        }

        // Stop observing firebase sleep state
        viewModel.run {
            getLastSleepStateResult().removeObservers(
                viewLifecycleOwner
            )
            stopObservingLastSleepState()
        }
    }

    companion object {
        private val TAG: String = BabyBoardFragment::class.java.simpleName
    }
}