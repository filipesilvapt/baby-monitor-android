package com.babyMonitor.models

import com.google.firebase.database.PropertyName

class SleepDeviationValue {

    @get:PropertyName("type_of_deviation")
    @set:PropertyName("type_of_deviation")
    var typeOfDeviation: Int = 0

    @get:PropertyName("deviation")
    @set:PropertyName("deviation")
    var deviation: Double = 0.0

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: String = ""

    override fun toString(): String {
        return "Deviation Type: $typeOfDeviation Value: $deviation Timestamp: $timestamp"
    }
}