package com.babyMonitor.models

import com.babyMonitor.utils.Utils
import com.google.gson.annotations.SerializedName

class AccelerometerValue {

    @SerializedName("x_axis")
    val xAxis: Double = 0.0

    @SerializedName("y_axis")
    val yAxis: Double = 0.0

    @SerializedName("z_axis")
    val zAxis: Double = 0.0

    val timestamp: String = ""

    override fun toString(): String {
        return "X: ${Utils.getDoubleOneDecimal(xAxis)}" +
                " Y: ${Utils.getDoubleOneDecimal(yAxis)}" +
                " Z: ${Utils.getDoubleOneDecimal(zAxis)} Timestamp: $timestamp"
    }
}