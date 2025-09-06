package com.example.airportactivity.models;

data class FlightCacheEntry(
    val flights: MutableList<FlightInfo> = mutableListOf(),
    var lastCursor: String? = null,
    var hasMore: Boolean = true,
    var lastFetchTime: Long = System.currentTimeMillis()
)
