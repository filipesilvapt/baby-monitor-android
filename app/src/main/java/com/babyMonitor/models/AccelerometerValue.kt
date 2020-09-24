package com.babyMonitor.models

import com.babyMonitor.utils.Utils

class AccelerometerValue {

    val x_axis: Double = 0.0
    val y_axis: Double = 0.0
    val z_axis: Double = 0.0

    val timestamp: String = ""

    override fun toString(): String {
        return "X: ${Utils.getDoubleOneDecimal(x_axis)}" +
                " Y: ${Utils.getDoubleOneDecimal(y_axis)}" +
                " Z: ${Utils.getDoubleOneDecimal(z_axis)} Timestamp: $timestamp"
    }
}