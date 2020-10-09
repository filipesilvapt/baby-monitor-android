package com.babyMonitor.utils

object Constants {

    /**
     * This defines the maximum time in milliseconds that the baby status should be considered active
     * without receiving any new data
     */
    const val BABY_STATUS_AVAILABILITY_MAX_DELAY: Long = 40000L
}