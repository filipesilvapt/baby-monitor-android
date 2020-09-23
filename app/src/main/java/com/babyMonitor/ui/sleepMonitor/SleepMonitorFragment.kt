package com.babyMonitor.ui.sleepMonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.babyMonitor.R

class SleepMonitorFragment : Fragment() {

    private lateinit var sleepMonitorViewModel: SleepMonitorViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        sleepMonitorViewModel =
                ViewModelProviders.of(this).get(SleepMonitorViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_sleep_monitor, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        sleepMonitorViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}