package com.babyMonitor.utils

object Constants {

    /**
     * This defines the maximum time in milliseconds that the baby status should be considered active
     * without receiving any new data
     */
    const val BABY_STATUS_AVAILABILITY_MAX_DELAY: Long = 40000L

    /**
     * Default value for the high temperature threshold, considered fever
     */
    const val HIGH_TEMPERATURE_DEFAULT_THRESHOLD: Double = 37.5

    /**
     * Default value for the low temperature threshold, considered cold
     */
    const val LOW_TEMPERATURE_DEFAULT_THRESHOLD: Double = 36.0
}