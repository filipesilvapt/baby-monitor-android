package com.babyMonitor.models

import com.babyMonitor.utils.Utils
import com.google.firebase.database.PropertyName

data class TemperatureThresholds(
    @get:PropertyName("high_temp")
    @set:PropertyName("high_temp")
    var highTemp: Double = 0.0,

    @get:PropertyName("low_temp")
    @set:PropertyName("low_temp")
    var lowTemp: Double = 0.0
) {
    override fun toString(): String {
        return "(Thresholds) High Temp: ${Utils.getDoubleToStringWithOneDecimal(highTemp)} ºC" +
                " Low Temp: ${Utils.getDoubleToStringWithOneDecimal(lowTemp)} ºC"
    }
}