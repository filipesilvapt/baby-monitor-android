package com.babyMonitor.models

import com.babyMonitor.utils.Utils
import com.google.gson.annotations.SerializedName

class TemperatureThresholds {

    @SerializedName("high_temp")
    val highTemp: Double = 0.0

    @SerializedName("low_temp")
    val lowTemp: Double = 0.0

    override fun toString(): String {
        return "(Thresholds) High Temp: ${Utils.getDoubleOneDecimal(highTemp)} ºC" +
                " Low Temp: ${Utils.getDoubleOneDecimal(lowTemp)} ºC"
    }
}