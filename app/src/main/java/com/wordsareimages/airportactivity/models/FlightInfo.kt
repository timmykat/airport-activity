package com.wordsareimages.airportactivity.models

import java.time.ZonedDateTime

data class FlightInfo(
    val ident: String,
    val aircraftType: String,
    val estimatedOn: String,
    val estimatedOff: String,
    val timestamp: ZonedDateTime,
    val operation: String) {

        companion object {
            fun fromJson(json: org.json.JSONObject, operation: String): FlightInfo {
                val ident = json.optString("ident", "N/A")
                val aircraftType = json.optString("aircraft_type", "Unknown")
                val estimatedOn = json.optString("estimated_on", "")
                val estimatedOff = json.optString("estimated_off", "")

                val timestampStr =
                    json.optString("scheduled_out", "") // fallback to "scheduled_in" if needed

                val timestamp = try {
                    ZonedDateTime.parse(timestampStr)
                } catch (e: Exception) {
                    ZonedDateTime.now() // fallback
                }

                return FlightInfo(
                    ident = ident,
                    aircraftType = aircraftType,
                    estimatedOn = estimatedOn,
                    estimatedOff = estimatedOff,
                    timestamp = timestamp,
                    operation = operation
                )
            }
        }
}
