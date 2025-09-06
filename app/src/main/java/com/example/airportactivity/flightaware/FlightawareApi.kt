package com.example.airportactivity.flightaware

import com.example.airportactivity.models.FlightInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object FlightawareApi {
    private val client = OkHttpClient()

    private const val API_KEY = BuildConfig.MY_API_KEY

    private const val BASE_URL = "https://aeroapi.flightaware.com/aeroapi/airports/#AIRPORT#/flights"

    suspend fun getApiData(airportCode: String, type: String): List<FlightInfo> {
        return when (type.lowercase()) {
            "arrivals" -> getFlightData(airportCode, "arrivals", 10)
            "departures" -> getFlightData(airportCode, "departures", 10)
            "all" -> {
                val arrivals = getFlightData(airportCode, "arrivals")
                val departures = getFlightData(airportCode, "departures")
                arrivals + departures
            }

            else -> throw IllegalArgumentException("Invalid type: $type")
        }
    }

    suspend fun getFlightData(
        airportCode: String,
        type: String,
        maxPages: Int = 10
    ): List<FlightInfo> = withContext(Dispatchers.IO) {

        val typePath = when (type.lowercase()) {
            "arrivals" -> "scheduled_arrivals"
            "departures" -> "scheduled_departures"
            else -> throw IllegalArgumentException("Invalid type: $type")
        }

        val label = if (type.lowercase() == "arrivals") "Arr" else "Dep"
        val allFlights = mutableListOf<FlightInfo>()
        var cursor: String? = null
        var pagesFetched = 0
        val endDate = ZonedDateTime.now().plusDays(2)
        val endDateFormatted = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(endDate)

        val basePath = BASE_URL.replace("#AIRPORT#", airportCode) + "/$typePath"

        while (pagesFetched < maxPages) {
            val urlBuilder = StringBuilder(basePath)
            urlBuilder.append("?end=$endDateFormatted&max_pages=1") // 1 page per request

            if (cursor != null) {
                urlBuilder.append("&cursor=$cursor")
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .addHeader("x-apikey", API_KEY)
                .build()

            var nextCursor: String? = null
            var shouldStop = false

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Error: ${response.code} ${response.message}")
                }

                val body = response.body?.string() ?: throw Exception("Empty response")
                val json = JSONObject(body)

                val flightsArray = json.optJSONArray(typePath)
                if (flightsArray == null || flightsArray.length() == 0) {
                    shouldStop = true
                    return@use
                }

                for (i in 0 until flightsArray.length()) {
                    // parse each flight and add to allFlights
                }

                val links = json.optJSONObject("links")
                nextCursor = links?.optString("next")?.takeIf { it.isNotBlank() }

                if (nextCursor == null) {
                    shouldStop = true
                }
            }

            // ✅ Now we can safely break or continue here
            if (shouldStop) break

            cursor = nextCursor
            pagesFetched++
        }

        return@withContext allFlights
    }

}