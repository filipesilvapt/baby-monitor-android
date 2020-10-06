package com.babyMonitor.database

object RTDatabasePaths {

    private const val DATABASE_NODE_ID: String = "node_01"

    const val PATH_CLIENT_TOKENS: String = "$DATABASE_NODE_ID/client_tokens"

    const val PATH_BABY_NAME: String = "$DATABASE_NODE_ID/baby_name"

    const val PATH_THERMOMETER_READINGS: String = "$DATABASE_NODE_ID/thermometer_readings"

    const val PATH_SLEEP_STATES: String = "$DATABASE_NODE_ID/sleep_states"

    const val PATH_TEMPERATURE_THRESHOLDS: String = "$DATABASE_NODE_ID/temperature_thresholds"
}