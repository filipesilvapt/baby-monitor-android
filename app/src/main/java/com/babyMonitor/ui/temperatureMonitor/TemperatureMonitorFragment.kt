package com.babyMonitor.ui.temperatureMonitor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.babyMonitor.MainApplication
import com.babyMonitor.R
import com.babyMonitor.charts.ChartYAxisLeftRenderer
import com.babyMonitor.charts.DateTimeValueFormatter
import com.babyMonitor.charts.TemperatureValueFormatter
import com.babyMonitor.models.TemperatureThresholds
import com.babyMonitor.models.ThermometerValue
import com.babyMonitor.utils.Utils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class TemperatureMonitorFragment : Fragment() {

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
        viewModel.temperatureHistory.observe(viewLifecycleOwner, Observer {
            populateTemperatureGraph(it)
        })

        // Observe firebase temperature history
        viewModel.observeFirebaseBabyTemperatureHistory()

        // Observe temperature thresholds
        MainApplication.instance.temperatureThresholds.observe(
            viewLifecycleOwner,
            { thresholds: TemperatureThresholds ->
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

        MainApplication.instance.temperatureThresholds.removeObservers(viewLifecycleOwner)

        viewModel.temperatureHistory.removeObservers(viewLifecycleOwner)

        viewModel.stopObservingFirebaseBabyTemperatureHistory()
    }

    private fun setupChartView() {
        // Enable touch gestures
        temperatureChart.setTouchEnabled(true)
        temperatureChart.dragDecelerationFrictionCoef = 0.9f

        // Enable scaling and dragging
        temperatureChart.isDragEnabled = true
        temperatureChart.setScaleEnabled(true)
        temperatureChart.setDrawGridBackground(false)
        temperatureChart.isHighlightPerDragEnabled = true

        // General chart customizations
        temperatureChart.apply {
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

            // todo try and rotate the temperature using a custom render
            rendererLeftYAxis =
                ChartYAxisLeftRenderer(viewPortHandler, axisLeft, rendererLeftYAxis.transformer)
        }

        // X Axis customizations -------
        temperatureChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = ContextCompat.getColor(requireContext(), R.color.colorChartAxisValues)
            textSize = 14f
            valueFormatter = DateTimeValueFormatter()
            labelRotationAngle = -45f
            granularity = 30000f
        }

        //  Y Axis (Left) customizations -------
        temperatureChart.axisLeft.apply {
            //setDrawLabels(true)
            //zeroLineWidth = 20f

            textColor = ContextCompat.getColor(requireContext(), R.color.colorChartAxisValues)
            textSize = 14f

            valueFormatter = TemperatureValueFormatter()

            granularity = 0.1f

            //axisMaximum = MAX_TEMPERATURE_VALUE
            //axisMinimum = MIN_TEMPERATURE_VALUE

            // Draw limit lines behind the main graph line or not
            //setDrawLimitLinesBehindData(true)
        }

    }

    private fun populateTemperatureGraph(temperatureHistory: List<ThermometerValue>) {
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

            if (thermometerValue.temp.toFloat() > maxTemperatureValueVisible) {
                isMaxTemperatureValueSurpassed = true
            } else if (thermometerValue.temp.toFloat() < minTemperatureValueVisible) {
                isMinTemperatureValueSurpassed = true
            }
        }

        temperatureChart.axisLeft.axisMaximum = maxTemperatureValueVisible.toFloat()
        temperatureChart.axisLeft.axisMinimum = minTemperatureValueVisible.toFloat()

        if (isMaxTemperatureValueSurpassed) temperatureChart.axisLeft.resetAxisMaximum()
        if (isMinTemperatureValueSurpassed) temperatureChart.axisLeft.resetAxisMinimum()

        val set1: LineDataSet
        /*if (temperatureChart.data != null &&
            temperatureChart.data.dataSetCount > 0
        ) {
            set1 = temperatureChart.getData().getDataSetByIndex(0)
            set1.values = values
            temperatureChart.getData().notifyDataChanged()
            temperatureChart.notifyDataSetChanged()
        } else {*/
        set1 = LineDataSet(values, "")

        set1.setDrawValues(false)
        set1.setDrawIcons(false)
        set1.color = ContextCompat.getColor(requireContext(), R.color.colorChartLine)
        set1.lineWidth = 4f

        set1.disableDashedLine()
        //set1.enableDashedLine(10f, 5f, 0f)
        //set1.enableDashedHighlightLine(10f, 5f, 0f)
        //set1.formLineWidth = 1f
        //set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        //set1.formSize = 15f

        set1.setDrawCircles(true)
        //set1.setCircleColor(Color.DKGRAY)
        //set1.circleRadius = 3f
        //set1.setDrawCircleHole(false)
        //set1.valueTextSize = 15f // points text size

        set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

        set1.setDrawFilled(false)
        //if (Utils.getSDKInt() >= 18) {
        //val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.side_nav_bar)
        //set1.fillDrawable = drawable
        //} else {
        //    set1.fillColor = Color.DKGRAY
        //}

        // Re-init value formatter with new base value
        temperatureChart.xAxis.valueFormatter =
            DateTimeValueFormatter(firstTemperatureReadingMillis)

        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(set1)
        val data = LineData(dataSets)
        temperatureChart.data = data

        // Refresh the chart with the new data
        temperatureChart.invalidate()
        //}
    }

    private fun updateGraphBoundaryValues(thresholds: TemperatureThresholds) {
        maxTemperatureValueVisible = thresholds.highTemp + BOUNDARY_DIFFERENCE_FROM_THRESHOLD
        minTemperatureValueVisible = thresholds.lowTemp - BOUNDARY_DIFFERENCE_FROM_THRESHOLD
    }

    private fun updateLimitLines(thresholds: TemperatureThresholds) {
        Log.i(TAG, "Updating limit lines with thresholds: $thresholds")

        temperatureChart.axisLeft.apply {
            val ll1 = LimitLine(
                thresholds.highTemp.toFloat(),
                getString(R.string.chart_limit_temperature_max)
            )
            ll1.lineColor = ContextCompat.getColor(requireContext(), R.color.colorChartMaxValue)
            ll1.lineWidth = 4f
            //ll1.enableDashedLine(5f, 10f, 0f)
            ll1.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
            ll1.textSize = 17f
            ll1.textColor = ContextCompat.getColor(requireContext(), R.color.colorChartMaxValue)

            val ll2 = LimitLine(
                thresholds.lowTemp.toFloat(),
                getString(R.string.chart_limit_temperature_min)
            )
            ll2.lineColor = ContextCompat.getColor(requireContext(), R.color.colorChartMinValue)
            ll2.lineWidth = 4f
            //ll2.enableDashedLine(5f, 10f, 0f)
            ll2.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            ll2.textSize = 17f
            ll2.textColor = ContextCompat.getColor(requireContext(), R.color.colorChartMinValue)

            removeAllLimitLines()
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