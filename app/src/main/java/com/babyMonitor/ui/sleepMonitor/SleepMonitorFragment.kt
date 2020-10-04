package com.babyMonitor.ui.sleepMonitor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.babyMonitor.R
import com.babyMonitor.charts.AxisValueFormatter
import com.babyMonitor.charts.DateTimeValueFormatter
import com.babyMonitor.charts.SleepDeviationsRenderer
import com.babyMonitor.models.SleepDeviationValue
import com.babyMonitor.utils.Utils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BubbleChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.BubbleDataSet
import com.github.mikephil.charting.data.BubbleEntry
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet

class SleepMonitorFragment : Fragment() {

    private lateinit var sleepMonitorViewModel: SleepMonitorViewModel

    private lateinit var sleepStateChart: BubbleChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView - Starting observers")

        sleepMonitorViewModel =
            ViewModelProvider(this).get(SleepMonitorViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_sleep_monitor, container, false)

        sleepStateChart = root.findViewById(R.id.chart_sleep_state)

        setupChart()

        sleepMonitorViewModel.sleepDeviationsHistory.observe(viewLifecycleOwner, Observer {
            populateSleepStateGraph(it)
        })

        sleepMonitorViewModel.observeFirebaseSleepDeviationsHistory()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView - Stopping observers")
        sleepMonitorViewModel.sleepDeviationsHistory.removeObservers(viewLifecycleOwner)
        sleepMonitorViewModel.stopObservingFirebaseSleepDeviationsHistory()
    }

    private fun setupChart() {
        // General chart customizations
        sleepStateChart.apply {
            // Enable touch gestures
            setTouchEnabled(true)
            dragDecelerationFrictionCoef = 0.9f

            // Enable scaling and dragging
            isDragEnabled = true
            setScaleEnabled(true)
            setDrawGridBackground(false)
            isHighlightPerDragEnabled = true

            renderer = SleepDeviationsRenderer(this, animator, viewPortHandler)

            // Hide the right axis values
            axisRight.isEnabled = false

            // Hide the X Axis description
            description.isEnabled = false

            // Hide the LineDataSet label
            legend.isEnabled = true

            // Set a bottom offset as the values get cut off without the LineDataSet label
            setExtraOffsets(0f, 0f, 20f, 2f)

            // Animate the graph lines when entering
            animateX(1000, Easing.Linear)
        }

        // X Axis customizations -------
        sleepStateChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = ContextCompat.getColor(requireContext(), R.color.colorChartAxisValues)
            textSize = 14f
            valueFormatter = DateTimeValueFormatter()
            labelRotationAngle = -45f
            granularity = 10000f
        }

        //  Y Axis (Left) customizations -------
        sleepStateChart.axisLeft.apply {
            textColor = ContextCompat.getColor(requireContext(), R.color.colorChartAxisValues)
            textSize = 14f

            valueFormatter = AxisValueFormatter()

            granularity = 0.1f
        }

    }

    private fun populateSleepStateGraph(sleepDeviationsHistory: List<SleepDeviationValue>) {
        if (sleepDeviationsHistory.isEmpty()) return

        val deviationsTypeA: ArrayList<BubbleEntry> = ArrayList()
        val deviationsTypeB: ArrayList<BubbleEntry> = ArrayList()

        // Get the first reading as the reference value to lower every X axis value in order for
        // the library to be able to manage them.
        // REF: https://github.com/PhilJay/MPAndroidChart/issues/789#issuecomment-241507904
        val firstDeviationMillis: Long = Utils.getDateInMilliSeconds(
            sleepDeviationsHistory[0].timestamp,
            Utils.FORMAT_DATE_AND_TIME
        )

        var entryTimestamp: Float

        sleepDeviationsHistory.forEach { deviationValue ->
            entryTimestamp = (Utils.getDateInMilliSeconds(
                deviationValue.timestamp,
                Utils.FORMAT_DATE_AND_TIME
            ) - firstDeviationMillis).toFloat()

            if (deviationValue.typeOfDeviation == DEVIATION_TYPE_INTERMEDIARY) {
                deviationsTypeA.add(
                    BubbleEntry(
                        // date and time in milliseconds minus the reference value
                        entryTimestamp,
                        // deviation reading
                        deviationValue.deviation.toFloat(),
                        120f
                        /*ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_emotion_state_agitated
                        )*/
                    )
                )
            } else if (deviationValue.typeOfDeviation == DEVIATION_TYPE_NOTIFICATION) {
                deviationsTypeB.add(
                    BubbleEntry(
                        // date and time in milliseconds minus the reference value
                        entryTimestamp,
                        // deviation reading
                        deviationValue.deviation.toFloat(),
                        120f
                        /*ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_emotion_state_disturbed
                        )*/
                    )
                )
            }
        }

        val set1 = BubbleDataSet(deviationsTypeA, "Deviations").apply {
            setDrawValues(true)
            setDrawIcons(true)
            color = ContextCompat.getColor(requireContext(), R.color.colorChartLine)
            isNormalizeSizeEnabled = false
        }

        val set2 = BubbleDataSet(deviationsTypeB, "Notifications").apply {
            setDrawValues(true)
            setDrawIcons(true)
            color = ContextCompat.getColor(requireContext(), R.color.colorChartMinValue)
            isNormalizeSizeEnabled = false
        }

        // Re-init value formatter with new base value
        sleepStateChart.xAxis.valueFormatter =
            DateTimeValueFormatter(firstDeviationMillis)

        val dataSets: ArrayList<IBubbleDataSet> = ArrayList()
        dataSets.add(set1)
        dataSets.add(set2)

        val data = BubbleData(dataSets)
        data.setDrawValues(true)
        data.setHighlightCircleWidth(1.5f)
        sleepStateChart.data = data

        // Refresh the chart with the new data
        sleepStateChart.invalidate()
    }

    companion object {
        private val TAG: String = SleepMonitorFragment::class.java.simpleName

        private const val DEVIATION_TYPE_INTERMEDIARY: Int = 1
        private const val DEVIATION_TYPE_NOTIFICATION: Int = 2
    }

}