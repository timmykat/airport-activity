package com.wordsareimages.airportactivity.flightaware

import com.wordsareimages.airportactivity.models.FlightInfo
import com.wordsareimages.airportactivity.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.Instant
import android.util.Log;
import okhttp3.HttpUrl.Companion.toHttpUrl

object FlightawareApi {
    private val client = OkHttpClient()

    private const val API_BASE = "https://aeroapi.flightaware.com/aeroapi"

    private const val SCHEME = "https"
    private const val HOST = "aeroapi.flightaware.com"
    private const val BASE_PATH = "aeroapi/airports"

    suspend fun getApiData(airportCode: String, type: String): List<FlightInfo> {
        return when (type.lowercase()) {
            "arrivals" -> getFlightData(airportCode, "arrivals", 10)
            "departures" -> getFlightData(airportCode, "departures", 10)
            "all" -> {
                val arrivals = getFlightData(airportCode, "arrivals", 10)
                val departures = getFlightData(airportCode, "departures", 10)
                arrivals + departures
            }

            else -> throw IllegalArgumentException("Invalid type: $type")
        }
    }

    suspend fun getFlightData(
        airportCode: String,
        type: String,
        maxPages: Int = 1
    ): List<FlightInfo> = withContext(Dispatchers.IO) {

        val typePath = when (type.lowercase()) {
            "arrivals" -> "scheduled_arrivals"
            "departures" -> "scheduled_departures"
            else -> throw IllegalArgumentException("Invalid type: $type")
        }

        Log.d("getFlightData", "Fetching $typePath")

        val allFlights = mutableListOf<FlightInfo>()
        var cursor: String? = null
        var pagesFetched = 0
        val startDate = Instant.now()
        val endDate = startDate.plusSeconds(3600 * 24) // 1 day
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .withZone(ZoneOffset.UTC)
        val startDateFormatted = formatter.format(startDate)
        val endDateFormatted = formatter.format(endDate)

        var url: HttpUrl? = null
        while (pagesFetched < maxPages) {

            if (url == null) {
                val urlBuilder = HttpUrl.Builder()
                    .scheme(SCHEME)
                    .host(HOST)
                    .addPathSegments(BASE_PATH)
                    .addPathSegment(airportCode)
                    .addPathSegment("flights")
                    .addPathSegment(typePath)

                urlBuilder.addQueryParameter("start", startDateFormatted)
                    .addQueryParameter("end", endDateFormatted)
                    .addQueryParameter("max_pages", "1") // 1 page per request

                url = urlBuilder.build()
            }

            Log.d("apiCall", "URL: $url")
            val request = Request.Builder()
                .url(url)
                .addHeader("x-apikey", BuildConfig.FLIGHTAWARE_API_KEY)
                .build()

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
                    val flightJson = flightsArray.getJSONObject(i)
                    val flightInfo = FlightInfo.fromJson(flightJson, type)
                    allFlights.add(flightInfo)
                }

                val links = json.optJSONObject("links")
                val newPath = links?.optString("next")

                if (newPath == null) {
                    shouldStop = true
                } else {
                    url = "$API_BASE$newPath".toHttpUrl()
                }
            }

            // ✅ Now we can safely break or continue here
            if (shouldStop) break

            pagesFetched++
        }

        return@withContext allFlights
    }

}