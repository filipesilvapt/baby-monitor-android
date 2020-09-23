package com.babyMonitor.ui.babyBoard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.babyMonitor.R

class BabyBoardFragment : Fragment() {

    private lateinit var babyBoardViewModel: BabyBoardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView - Starting observers")

        babyBoardViewModel = ViewModelProviders.of(this).get(BabyBoardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_baby_board, container, false)

        val textViewBabyName: TextView = root.findViewById(R.id.text_baby_name)
        babyBoardViewModel.textBabyName.observe(viewLifecycleOwner, Observer {
            textViewBabyName.text = it
        })

        // Observe firebase baby name
        babyBoardViewModel.observeFirebaseBabyName()

        // Temperature text
        val textViewBabyTemperature: TextView = root.findViewById(R.id.text_baby_temperature)
        babyBoardViewModel.textBabyTemperature.observe(viewLifecycleOwner, Observer {
            textViewBabyTemperature.text = it
        })

        // Temperature image
        val imageViewBabyTemperature: ImageView = root.findViewById(R.id.image_baby_temperature)
        babyBoardViewModel.imageBabyTemperatureResId.observe(viewLifecycleOwner, Observer {
            it?.let {
                imageViewBabyTemperature.background =
                    ContextCompat.getDrawable(requireContext(), it)
            }
        })

        // Observe firebase temperature
        babyBoardViewModel.observeFirebaseBabyTemperature()

        // Sleep state text
        val textViewBabySleepState: TextView = root.findViewById(R.id.text_baby_sleep_state)
        babyBoardViewModel.textBabySleepStateResId.observe(viewLifecycleOwner, Observer {
            it?.let { textViewBabySleepState.text = getString(it) }
        })

        // Sleep state image
        val imageViewBabySleepState: ImageView = root.findViewById(R.id.image_baby_sleep_state)
        babyBoardViewModel.imageBabySleepStateResId.observe(viewLifecycleOwner, Observer {
            it?.let {
                imageViewBabySleepState.background = ContextCompat.getDrawable(requireContext(), it)
            }
        })

        // Observer firebase sleep state
        babyBoardViewModel.observeFirebaseBabySleepState()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView - Stopping observers")

        babyBoardViewModel.textBabyName.removeObservers(viewLifecycleOwner)
        babyBoardViewModel.stopObservingFirebaseBabyName()

        babyBoardViewModel.textBabyTemperature.removeObservers(viewLifecycleOwner)
        babyBoardViewModel.imageBabyTemperatureResId.removeObservers(viewLifecycleOwner)
        babyBoardViewModel.stopObservingFirebaseBabyTemperature()

        babyBoardViewModel.textBabySleepStateResId.removeObservers(viewLifecycleOwner)
        babyBoardViewModel.imageBabySleepStateResId.removeObservers(viewLifecycleOwner)
        babyBoardViewModel.stopObservingFirebaseBabySleepState()
    }

    companion object {
        private val TAG: String = BabyBoardFragment::class.java.simpleName
    }
}