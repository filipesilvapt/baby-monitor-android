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
import com.babyMonitor.models.AccelerometerValue
import com.babyMonitor.utils.Utils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class SleepMonitorFragment : Fragment() {

    private lateinit var sleepMonitorViewModel: SleepMonitorViewModel

    private lateinit var sleepStateChart: LineChart

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

        sleepMonitorViewModel.accelerometerHistory.observe(viewLifecycleOwner, Observer {
            populateSleepStateGraph(it)
        })

        sleepMonitorViewModel.observeFirebaseBabyAccelerometerHistory()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView - Stopping observers")
        sleepMonitorViewModel.accelerometerHistory.removeObservers(viewLifecycleOwner)
        sleepMonitorViewModel.stopObservingFirebaseBabyAccelerometerHistory()
    }

    private fun setupChart() {
        // Enable touch gestures
        sleepStateChart.setTouchEnabled(true)
        sleepStateChart.dragDecelerationFrictionCoef = 0.9f

        // Enable scaling and dragging
        sleepStateChart.isDragEnabled = true
        sleepStateChart.setScaleEnabled(true)
        sleepStateChart.setDrawGridBackground(false)
        sleepStateChart.isHighlightPerDragEnabled = true

        // General chart customizations
        sleepStateChart.apply {
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
            granularity = 200f
        }

        //  Y Axis (Left) customizations -------
        sleepStateChart.axisLeft.apply {
            textColor = ContextCompat.getColor(requireContext(), R.color.colorChartAxisValues)
            textSize = 14f

            valueFormatter = AxisValueFormatter()

            granularity = 0.1f
        }

    }

    private fun populateSleepStateGraph(accelerometerHistory: List<AccelerometerValue>) {
        if (accelerometerHistory.isEmpty()) return

        val valuesX: ArrayList<Entry> = ArrayList()
        val valuesY: ArrayList<Entry> = ArrayList()
        val valuesZ: ArrayList<Entry> = ArrayList()

        // Get the first reading as the reference value to lower every X axis value in order for
        // the library to be able to manage them.
        // REF: https://github.com/PhilJay/MPAndroidChart/issues/789#issuecomment-241507904
        val firstAccelerometerReadingMillis: Long = Utils.getDateInMilliSeconds(
            accelerometerHistory[0].timestamp,
            Utils.FORMAT_DATE_AND_TIME
        )

        var entryTimestamp: Float

        accelerometerHistory.forEach { accelerometerValue ->
            entryTimestamp = (Utils.getDateInMilliSeconds(
                accelerometerValue.timestamp,
                Utils.FORMAT_DATE_AND_TIME
            ) - firstAccelerometerReadingMillis).toFloat()

            valuesX.add(
                Entry(
                    // date and time in milliseconds minus the reference value
                    entryTimestamp,
                    // accelerometer reading
                    accelerometerValue.x_axis.toFloat()
                )
            )

            valuesY.add(
                Entry(
                    // date and time in milliseconds minus the reference value
                    entryTimestamp,
                    // accelerometer reading
                    accelerometerValue.y_axis.toFloat()
                )
            )

            valuesZ.add(
                Entry(
                    // date and time in milliseconds minus the reference value
                    entryTimestamp,
                    // accelerometer reading
                    accelerometerValue.z_axis.toFloat()
                )
            )
        }

        val set1: LineDataSet
        set1 = LineDataSet(valuesX, "X Axis")
        set1.setDrawValues(false)
        set1.setDrawIcons(false)
        set1.color = ContextCompat.getColor(requireContext(), R.color.colorChartLine)
        set1.lineWidth = 4f
        set1.disableDashedLine()
        set1.setDrawCircles(false)
        set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        set1.setDrawFilled(false)

        val set2: LineDataSet
        set2 = LineDataSet(valuesY, "Y Axis")
        set2.setDrawValues(false)
        set2.setDrawIcons(false)
        set2.color = ContextCompat.getColor(requireContext(), R.color.colorChartMinValue)
        set2.lineWidth = 4f
        set2.disableDashedLine()
        set2.setDrawCircles(false)
        set2.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        set2.setDrawFilled(false)

        val set3: LineDataSet
        set3 = LineDataSet(valuesZ, "Z Axis")
        set3.setDrawValues(false)
        set3.setDrawIcons(false)
        set3.color = ContextCompat.getColor(requireContext(), R.color.colorChartMaxValue)
        set3.lineWidth = 4f
        set3.disableDashedLine()
        set3.setDrawCircles(false)
        set3.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        set3.setDrawFilled(false)

        // Re-init value formatter with new base value
        sleepStateChart.xAxis.valueFormatter =
            DateTimeValueFormatter(firstAccelerometerReadingMillis)

        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(set1)
        dataSets.add(set2)
        dataSets.add(set3)

        val data = LineData(dataSets)
        sleepStateChart.data = data

        // Refresh the chart with the new data
        sleepStateChart.invalidate()
    }

    companion object {
        private val TAG: String = SleepMonitorFragment::class.java.simpleName
    }

}