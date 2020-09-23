package com.babyMonitor.models

import com.babyMonitor.Utils

class ThermometerValue {

    val temp: Double = 0.0

    val timestamp: String = ""

    override fun toString(): String {
        return "Temp: ${Utils.getDoubleOneDecimal(temp)} ºC Timestamp: $timestamp"
    }
}