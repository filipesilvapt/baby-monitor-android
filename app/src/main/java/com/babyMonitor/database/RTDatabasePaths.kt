package com.babyMonitor.database

object RTDatabasePaths {

    private const val DATABASE_NODE_ID: String = "Node-02"

    const val PATH_CLIENT_TOKENS: String = "$DATABASE_NODE_ID/ClientTokens"

    const val PATH_BABY_NAME: String = "$DATABASE_NODE_ID/BabyName"

    const val PATH_THERMOMETER_LAST_READING: String = "$DATABASE_NODE_ID/LastThermometerReading"

    const val PATH_LAST_SLEEP_STATE: String = "$DATABASE_NODE_ID/LastSleepState"

    const val PATH_THERMOMETER_READINGS: String = "$DATABASE_NODE_ID/ThermometerReadings"
}