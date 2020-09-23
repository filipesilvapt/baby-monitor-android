package com.babyMonitor.charts

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*


class DateTimeValueFormatter(private val referenceValueToSum: Long = 0) : ValueFormatter() {
    private val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // override this for e.g. LineChart or ScatterChart
    override fun getPointLabel(entry: Entry?): String {
        return format.format(Date(entry?.y!!.toLong() + referenceValueToSum))
    }

    // override this for BarChart
    override fun getBarLabel(barEntry: BarEntry?): String {
        return format.format(Date(barEntry?.y!!.toLong() + referenceValueToSum))
    }

    // override this for custom formatting of XAxis or YAxis labels
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return format.format(Date(value.toLong() + referenceValueToSum))
    }
    // ... override other methods for the other chart types

}