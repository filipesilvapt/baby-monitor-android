package com.babyMonitor.charts

import android.util.Log
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.BubbleDataProvider
import com.github.mikephil.charting.renderer.BubbleChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class SleepDeviationsRenderer(
    chart: BubbleDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BubbleChartRenderer(chart, animator, viewPortHandler) {

    override fun getShapeSize(
        entrySize: Float,
        maxSize: Float,
        reference: Float,
        normalizeSize: Boolean
    ): Float {
       val finalValue: Float  = super.getShapeSize(entrySize, maxSize, reference, normalizeSize)
        Log.d("TESTSHAPE", "Entry size: $entrySize Max size: $maxSize Reference: $reference Normalize Size: $normalizeSize Final Value: $finalValue")
        return entrySize
    }
}