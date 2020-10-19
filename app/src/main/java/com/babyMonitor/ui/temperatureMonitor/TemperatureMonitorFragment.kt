package com.babyMonitor.ui.temperatureMonitor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.babyMonitor.R
import com.babyMonitor.charts.DateTimeValueFormatter
import com.babyMonitor.charts.TemperatureValueFormatter
import com.babyMonitor.models.TemperatureThresholdsModel
import com.babyMonitor.models.ThermometerModel
import com.babyMonitor.repositories.TemperatureThresholdsRepository
import com.babyMonitor.utils.Utils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TemperatureMonitorFragment : Fragment() {

    @Inject
    lateinit var temperatureThresholdsRepository: TemperatureThresholdsRepository

    private lateinit var viewModel: TemperatureMonitorViewModel

    private lateinit var temperatureChart: LineChart

    private var maxTemperatureValueVisible: Double = 0.0

    private var minTemperatureValueVisible: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView")

        viewModel = ViewModelProvider(this).get(TemperatureMonitorViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_temperature_monitor, container, false)

        temperatureChart = root.findViewById(R.id.chart_temperature)

        // Setup the chart view
        setupChartView()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated - Starting observers")

        // Observe the temperature history list
        viewModel.temperatureHistory.observe(viewLifecycleOwner, {
            populateTemperatureGraph(it)
        })

        // Observe firebase temperature history
        viewModel.observeFirebaseBabyTemperatureHistory()

        // Observe temperature thresholds
        temperatureThresholdsRepository.temperatureThresholds.observe(
            viewLifecycleOwner,
            { thresholds: TemperatureThresholdsModel ->
                run {
                    updateGraphBoundaryValues(thresholds)
                    updateLimitLines(thresholds)
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(TAG, "onDestroyView - Stopping observers")

        temperatureThresholdsRepository.temperatureThresholds.removeObservers(
            viewLifecycleOwner
        )

        viewModel.temperatureHistory.removeObservers(viewLifecycleOwner)

        viewModel.stopObservingFirebaseBabyTemperatureHistory()
    }

    private fun setupChartView() {
        // General chart customizations
        temperatureChart.apply {
            // Enable touch gestures
            setTouchEnabled(true)
            dragDecelerationFrictionCoef = 0.9f

            // Enable scaling and dragging
            isDragEnabled = true
            setScaleEnabled(true)
            setDrawGridBackground(false)
            isHighlightPerDragEnabled = true

            // Hide the right axis values
            axisRight.isEnabled = false

            // Hide the X Axis description
            description.isEnabled = false

            // Hide the LineDataSet label
            legend.isEnabled = false

            // Set a bottom offset as the values get cut off without the LineDataSet label
            setExtraOffsets(0f, 0f, 20f, 2f)

            // Animate the graph lines when entering
            animateX(1000, Easing.Linear)
        }

        // X Axis customizations
        temperatureChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = ContextCompat.getColor(requireContext(), R.color.colorChartAxisValues)
            textSize = 14f
            valueFormatter = DateTimeValueFormatter()
            labelRotationAngle = -45f
            granularity = 30000f
        }

        //  Y Axis (Left) customizations
        temperatureChart.axisLeft.apply {
            textColor = ContextCompat.getColor(requireContext(), R.color.colorChartAxisValues)
            textSize = 14f

            // Add custom formatter to only show one decimal place
            valueFormatter = TemperatureValueFormatter()

            // Set the granularity to the decimal place
            granularity = 0.1f

            // Draw limit lines behind the main graph line or not
            setDrawLimitLinesBehindData(false)
        }

    }

    /**
     * Populate the chart with a list of thermometer values that contain the temperature and the
     * timestamp at which the reading was made
     */
    private fun populateTemperatureGraph(temperatureHistory: List<ThermometerModel>) {
        if (temperatureHistory.isEmpty()) return

        val values: ArrayList<Entry> = ArrayList()

        var isMaxTemperatureValueSurpassed = false
        var isMinTemperatureValueSurpassed = false

        // Get the first reading as the reference value to lower every X axis value in order for
        // the library to be able to manage them.
        // REF: https://github.com/PhilJay/MPAndroidChart/issues/789#issuecomment-241507904
        val firstTemperatureReadingMillis: Long = Utils.convertDateToMillis(
            temperatureHistory[0].timestamp,
            Utils.FORMAT_DATE_AND_TIME
        )

        // Go through each thermometer value and create a valid graph entry
        temperatureHistory.forEach { thermometerValue ->
            values.add(
                Entry(
                    // date and time in milliseconds minus the reference value
                    (Utils.convertDateToMillis(
                        thermometerValue.timestamp,
                        Utils.FORMAT_DATE_AND_TIME
                    ) - firstTemperatureReadingMillis).toFloat(),
                    // temperature reading
                    thermometerValue.temp.toFloat()
                )
            )

            // Check which graph boundaries we will need to suppress depending if we have temperatures
            // that surpass them or not
            if (thermometerValue.temp.toFloat() > maxTemperatureValueVisible) {
                isMaxTemperatureValueSurpassed = true
            } else if (thermometerValue.temp.toFloat() < minTemperatureValueVisible) {
                isMinTemperatureValueSurpassed = true
            }
        }

        // Update the boundaries values with the calculated boundaries
        temperatureChart.axisLeft.axisMaximum = maxTemperatureValueVisible.toFloat()
        temperatureChart.axisLeft.axisMinimum = minTemperatureValueVisible.toFloat()

        // Suppress the boundaries that we don't want
        if (isMaxTemperatureValueSurpassed) temperatureChart.axisLeft.resetAxisMaximum()
        if (isMinTemperatureValueSurpassed) temperatureChart.axisLeft.resetAxisMinimum()

        // Customize the data set with the graph values
        val set1: LineDataSet = LineDataSet(values, "").apply {
            setDrawValues(false)
            setDrawIcons(false)
            color = ContextCompat.getColor(requireContext(), R.color.colorChartLine)
            lineWidth = 4f

            disableDashedLine()

            setDrawCircles(true)

            mode = LineDataSet.Mode.HORIZONTAL_BEZIER

            setDrawFilled(false)
        }

        // Re-init value formatter with new base value
        temperatureChart.xAxis.valueFormatter =
            DateTimeValueFormatter(firstTemperatureReadingMillis)

        // Add the data to the chart
        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(set1)
        val data = LineData(dataSets)
        temperatureChart.data = data

        // Refresh the chart with the new data
        temperatureChart.invalidate()
    }

    /**
     * Update the values for the graph Y axis boundaries which determine the max and min temperatures
     * visible when the current temperature is within them
     */
    private fun updateGraphBoundaryValues(thresholds: TemperatureThresholdsModel) {
        maxTemperatureValueVisible = thresholds.highTemp + BOUNDARY_DIFFERENCE_FROM_THRESHOLD
        minTemperatureValueVisible = thresholds.lowTemp - BOUNDARY_DIFFERENCE_FROM_THRESHOLD
    }

    /**
     * Update the graph limit lines according to the given temperature thresholds that define
     * fever and cold
     */
    private fun updateLimitLines(thresholds: TemperatureThresholdsModel) {
        Log.i(TAG, "Updating limit lines with thresholds: $thresholds")

        temperatureChart.axisLeft.apply {
            val ll1 = LimitLine(
                thresholds.highTemp.toFloat(),
                getString(R.string.chart_limit_temperature_max)
            ).apply {
                lineColor = ContextCompat.getColor(requireContext(), R.color.colorChartMaxValue)
                lineWidth = 4f
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                textSize = 17f
                textColor = ContextCompat.getColor(requireContext(), R.color.colorChartMaxValue)
            }

            val ll2 = LimitLine(
                thresholds.lowTemp.toFloat(),
                getString(R.string.chart_limit_temperature_min)
            ).apply {
                lineColor = ContextCompat.getColor(requireContext(), R.color.colorChartMinValue)
                lineWidth = 4f
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                textSize = 17f
                textColor = ContextCompat.getColor(requireContext(), R.color.colorChartMinValue)
            }

            // Remove the previous limit lines
            removeAllLimitLines()

            // Add the new limit lines
            addLimitLine(ll1)
            addLimitLine(ll2)

            // Refresh the chart
            temperatureChart.invalidate()
        }
    }

    companion object {
        private val TAG: String = TemperatureMonitorFragment::class.java.simpleName

        private const val BOUNDARY_DIFFERENCE_FROM_THRESHOLD: Double = 0.5
    }

}