package com.babyMonitor.models

import com.google.firebase.database.PropertyName

class SleepStateModel(
    @get:PropertyName("state")
    @set:PropertyName("state")
    var state: Int = 0,

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: String = ""
) {
    override fun toString(): String {
        return "Sleep state: $state Timestamp: $timestamp"
    }
}