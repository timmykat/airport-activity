package com.example.airportactivity.flightaware

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import com.example.airportactivity.models.FlightInfo
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(name = "flight_prefs")

class FlightRepository(private val context: Context) {

    private val cacheKey = stringPreferencesKey("cached_flights")

    val cachedFlights: Flow<List<FlightInfo>> = context.dataStore.data
        .map { prefs ->
            val json = prefs[cacheKey] ?: "[]"
            Json.decodeFromString(json)
        }

    private suspend fun saveCache(flights: List<FlightInfo>) {
        val json = Json.encodeToString(flights)
        context.dataStore.edit { prefs ->
            prefs[cacheKey] = json
        }
    }

    // Fetch combined arrivals + departures and cache it
    suspend fun refreshFlights(airportCode: String): List<FlightInfo> {
        val flights = FlightawareApi.getApiData(airportCode, "all")
        saveCache(flights)
        return flights
    }
}