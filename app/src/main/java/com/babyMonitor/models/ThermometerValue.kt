package com.babyMonitor.models

import com.babyMonitor.utils.Utils
import com.google.firebase.database.PropertyName

class ThermometerValue(
    @get:PropertyName("temp")
    @set:PropertyName("temp")
    var temp: Double = 0.0,

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: String = ""
) {
    override fun toString(): String {
        return "Temp: ${Utils.getDoubleToStringWithOneDecimal(temp)} ºC Timestamp: $timestamp"
    }
}