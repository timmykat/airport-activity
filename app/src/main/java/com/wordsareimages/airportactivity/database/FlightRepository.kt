package com.wordsareimages.airportactivity.database

import FlightDao
import com.wordsareimages.airportactivity.models.FlightInfo
import java.time.ZonedDateTime

class FlightRepository(private val dao: FlightDao) {

    // Existing: get flights by operation
    fun getFlights(operation: String) = dao.getFlights(operation)

    // New: get all flights (Flow)
    fun getAllFlights() = dao.getAllFlights()

    // Insert a list of flights
    suspend fun insertFlights(flights: List<FlightInfo>) {
        dao.insertFlights(flights)
    }

    // Refresh flights with cleanup
    suspend fun refreshFlights(flights: List<FlightInfo>) {
        dao.insertFlights(flights)
        dao.deleteOldFlights(ZonedDateTime.now().minusDays(1))
    }

    // Clear all flights (if needed)
    suspend fun clearFlights() {
        dao.clearAllFlights()
    }
}
