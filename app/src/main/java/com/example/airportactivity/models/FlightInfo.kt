package com.example.airportactivity.models

import java.time.ZonedDateTime

data class FlightInfo(
    val ident: String,
    val aircraftType: String,
    val estimatedOn: String,
    val estimatedOff: String,
    val timestamp: ZonedDateTime,
    val operation: String
)
