package com.babyMonitor.models

import com.babyMonitor.utils.Utils
import com.google.firebase.database.PropertyName

class AccelerometerValue {

    @get:PropertyName("x_axis")
    @set:PropertyName("x_axis")
    var xAxis: Double = 0.0

    @get:PropertyName("y_axis")
    @set:PropertyName("y_axis")
    var yAxis: Double = 0.0

    @get:PropertyName("z_axis")
    @set:PropertyName("z_axis")
    var zAxis: Double = 0.0

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: String = ""

    override fun toString(): String {
        return "X: ${Utils.getDoubleToStringWithOneDecimal(xAxis)}" +
                " Y: ${Utils.getDoubleToStringWithOneDecimal(yAxis)}" +
                " Z: ${Utils.getDoubleToStringWithOneDecimal(zAxis)} Timestamp: $timestamp"
    }
}