package com.example.airportactivity.models

data class PagedFlightResult(
    val flights: List<FlightInfo>,
    val nextCursor: String?
)
