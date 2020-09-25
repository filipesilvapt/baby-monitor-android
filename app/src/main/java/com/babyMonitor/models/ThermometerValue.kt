package com.babyMonitor.models

import com.babyMonitor.utils.Utils
import com.google.gson.annotations.SerializedName

class ThermometerValue {

    @SerializedName("temp")
    val temp: Double = 0.0

    @SerializedName("timestamp")
    val timestamp: String = ""

    override fun toString(): String {
        return "Temp: ${Utils.getDoubleOneDecimal(temp)} ºC Timestamp: $timestamp"
    }
}