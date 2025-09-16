package com.wordsareimages.airportactivity.models

data class PagedFlightResult(
    val flights: List<FlightInfo>,
    val nextCursor: String?
)
