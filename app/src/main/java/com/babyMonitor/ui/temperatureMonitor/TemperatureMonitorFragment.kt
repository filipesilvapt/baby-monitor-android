package com.babyMonitor.ui.temperatureMonitor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.babyMonitor.R
import com.babyMonitor.Utils
import com.babyMonitor.charts.ChartYAxisLeftRenderer
import com.babyMonitor.charts.DateTimeValueFormatter
import com.babyMonitor.charts.TemperatureValueFormatter
import com.babyMonitor.models.ThermometerValue
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet


class TemperatureMonitorFragment : Fragment() {

    private lateinit var temperatureMonitorViewModel: TemperatureMonitorViewModel

    private lateinit var temperatureChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView - Starting observers")

        temperatureMonitorViewModel =
            ViewModelProviders.of(this).get(TemperatureMonitorViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_temperature_monitor, container, false)

        temperatureChart = root.findViewById(R.id.chart_temperature)

        setupChart()

        temperatureMonitorViewModel.temperatureHistory.observe(viewLifecycleOwner, Observer {
            populateTemperatureGraph(it)
        })

        temperatureMonitorViewModel.observeFirebaseBabyTemperatureHistory()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView - Stopping observers")
        temperatureMonitorViewModel.temperatureHistory.removeObservers(viewLifecycleOwner)
        temperatureMonitorViewModel.stopObservingFirebaseBabyTemperatureHistory()
    }

    private fun setupChart() {
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

            val ll1 = LimitLine(37.5f, getString(R.string.chart_limit_temperature_max))
            ll1.lineColor = ContextCompat.getColor(requireContext(), R.color.colorChartMaxValue)
            ll1.lineWidth = 4f
            //ll1.enableDashedLine(5f, 10f, 0f)
            ll1.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
            ll1.textSize = 17f
            ll1.textColor = ContextCompat.getColor(requireContext(), R.color.colorChartMaxValue)

            val ll2 = LimitLine(36f, getString(R.string.chart_limit_temperature_min))
            ll2.lineColor = ContextCompat.getColor(requireContext(), R.color.colorChartMinValue)
            ll2.lineWidth = 4f
            //ll2.enableDashedLine(5f, 10f, 0f)
            ll2.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            ll2.textSize = 17f
            ll2.textColor = ContextCompat.getColor(requireContext(), R.color.colorChartMinValue)

            removeAllLimitLines()
            addLimitLine(ll1)
            addLimitLine(ll2)
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
        val firstTemperatureReadingMillis: Long = Utils.getDateInMilliSeconds(
            temperatureHistory[0].timestamp,
            Utils.FORMAT_DATE_AND_TIME
        )

        temperatureHistory.forEach { thermometerValue ->
            values.add(
                Entry(
                    // date and time in milliseconds minus the reference value
                    (Utils.getDateInMilliSeconds(
                        thermometerValue.timestamp,
                        Utils.FORMAT_DATE_AND_TIME
                    ) - firstTemperatureReadingMillis).toFloat(),
                    // temperature reading
                    thermometerValue.temp.toFloat()
                )
            )

            if (thermometerValue.temp.toFloat() > MAX_TEMPERATURE_VALUE) {
                isMaxTemperatureValueSurpassed = true
            } else if (thermometerValue.temp.toFloat() < MIN_TEMPERATURE_VALUE) {
                isMinTemperatureValueSurpassed = true
            }
        }

        temperatureChart.axisLeft.axisMaximum = MAX_TEMPERATURE_VALUE
        temperatureChart.axisLeft.axisMinimum = MIN_TEMPERATURE_VALUE

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

        //temperatureChart.notifyDataSetChanged() // todo is required?

        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        dataSets.add(set1)
        val data = LineData(dataSets)
        temperatureChart.data = data

        // Refresh the chart with the new data
        temperatureChart.invalidate()
        //}
    }

    /*fun renderData(dates: List<String?>?, allAmounts: List<Double?>?) {
        val xAxisLabel: ArrayList<String> = ArrayList()
        xAxisLabel.add("1")
        xAxisLabel.add("7")
        xAxisLabel.add("14")
        xAxisLabel.add("21")
        xAxisLabel.add("28")
        xAxisLabel.add("30")
        val xAxis: XAxis = volumeReportChart.getXAxis()
        val position = XAxisPosition.BOTTOM
        xAxis.position = position
        xAxis.enableGridDashedLine(2f, 7f, 0f)
        xAxis.axisMaximum = 5f
        xAxis.axisMinimum = 0f
        xAxis.setLabelCount(6, true)
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 7f
        xAxis.labelRotationAngle = 315f
        xAxis.valueFormatter = ClaimsXAxisValueFormatter(dates)
        xAxis.setCenterAxisLabels(true)
        xAxis.setDrawLimitLinesBehindData(true)
        val ll1 = LimitLine(UISetters.getDateInNumber().toFloat(), UISetters.getDateInNumber())
        ll1.lineColor = resources.getColor(R.color.greyish_brown)
        ll1.lineWidth = 4f
        ll1.enableDashedLine(10f, 10f, 0f)
        ll1.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        ll1.textSize = 10f
        val ll2 = LimitLine(35f, "")
        ll2.lineWidth = 4f
        ll2.enableDashedLine(10f, 10f, 0f)
        ll2.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        ll2.textSize = 10f
        ll2.lineColor = Color.parseColor("#FFFFFF")
        xAxis.removeAllLimitLines()
        xAxis.addLimitLine(ll1)
        xAxis.addLimitLine(ll2)
        val leftAxis: YAxis = volumeReportChart.getAxisLeft()
        leftAxis.removeAllLimitLines()
        //leftAxis.addLimitLine(ll1);
        //leftAxis.addLimitLine(ll2);
        leftAxis.axisMaximum = findMaximumValueInList(allAmounts).floatValue() + 100f
        leftAxis.axisMinimum = 0f
        leftAxis.enableGridDashedLine(10f, 10f, 0f)
        leftAxis.setDrawZeroLine(false)
        leftAxis.setDrawLimitLinesBehindData(false)
        //XAxis xAxis = mBarChart.getXAxis();
        leftAxis.valueFormatter = ClaimsYAxisValueFormatter()
        volumeReportChart.getDescription().setEnabled(true)
        val description = Description()
        // description.setText(UISetters.getFullMonthName());//commented for weekly reporting
        description.text = "Week"
        description.textSize = 15f
        volumeReportChart.getDescription().setPosition(0f, 0f)
        volumeReportChart.setDescription(description)
        volumeReportChart.getAxisRight().setEnabled(false)

        //setData()-- allAmounts is data to display;
        setDataForWeeksWise(allAmounts)
    }

    private fun setDataForWeeksWise(amounts: List<Double>) {
        val values: ArrayList<Map.Entry<*, *>> = ArrayList()
        values.add(MutableMap.MutableEntry<Any?, Any?>(1, amounts[0].toFloat()))
        values.add(MutableMap.MutableEntry<Any?, Any?>(2, amounts[1].toFloat()))
        values.add(MutableMap.MutableEntry<Any?, Any?>(3, amounts[2].toFloat()))
        values.add(MutableMap.MutableEntry<Any?, Any?>(4, amounts[3].toFloat()))
        val set1: LineDataSet
        if (volumeReportChart.data != null &&
            volumeReportChart.data.dataSetCount > 0
        ) {
            set1 = volumeReportChart.data.getDataSetByIndex(0) as LineDataSet
            set1.setValues(values)
            volumeReportChart.data.notifyDataChanged()
            volumeReportChart.notifyDataSetChanged()
        } else {
            set1 = LineDataSet(values, "Total volume")
            set1.setDrawCircles(true)
            set1.enableDashedLine(10f, 0f, 0f)
            set1.enableDashedHighlightLine(10f, 0f, 0f)
            set1.color = resources.getColor(R.color.colorPrimary)
            set1.setCircleColor(resources.getColor(R.color.colorPrimary))
            set1.lineWidth = 2f //line size
            set1.circleRadius = 5f
            set1.setDrawCircleHole(true)
            set1.valueTextSize = 10f
            set1.setDrawFilled(true)
            set1.formLineWidth = 5f
            set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            set1.formSize = 5f
            //if (Utils.getSDKInt() >= 18) {
//                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.blue_bg);
//                set1.setFillDrawable(drawable);
                set1.fillColor = Color.WHITE
            }// else {
                set1.fillColor = Color.WHITE
            //}
            set1.setDrawValues(true)
            val dataSets: ArrayList<ILineDataSet> = ArrayList()
            dataSets.add(set1)
            val data = LineData(dataSets)
            volumeReportChart.data = data
        }
    }*/

    companion object {
        private val TAG: String = TemperatureMonitorFragment::class.java.simpleName
        private const val MAX_TEMPERATURE_VALUE = 38.0f
        private const val MIN_TEMPERATURE_VALUE = 35.5f
    }

}