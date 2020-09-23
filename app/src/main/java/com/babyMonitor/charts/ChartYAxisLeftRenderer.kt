package com.babyMonitor.charts

import android.graphics.Canvas
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

class ChartYAxisLeftRenderer(
    viewPortHandler: ViewPortHandler?,
    yAxis: YAxis?,
    trans: Transformer?
) : YAxisRenderer(viewPortHandler, yAxis, trans) {

    override fun drawYLabels(
        c: Canvas?,
        fixedPosition: Float,
        positions: FloatArray?,
        offset: Float
    ) {
        val from = if (mYAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
        val to =
            if (mYAxis.isDrawTopYLabelEntryEnabled) mYAxis.mEntryCount else mYAxis.mEntryCount - 1

        // draw
        for (i in from until to) {
            val text = mYAxis.getFormattedLabel(i)
            c!!.drawText(text, fixedPosition, positions!![i * 2 + 1] + offset, mAxisLabelPaint)
        }
    }
}