package com.wordsareimages.airportactivity.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wordsareimages.airportactivity.TwoDropdownsInline
import com.wordsareimages.airportactivity.database.FlightViewModel
import com.wordsareimages.airportactivity.flightaware.FlightawareApi
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun AirportActivityApp(viewModel: FlightViewModel) {
    val airports = listOf("KPVD", "KBOS")
    val days = listOf("Today", "Tomorrow")

    // Observing flights from Room via ViewModel
    val flights by viewModel.flights.collectAsState()

    var selectedAirport by remember { mutableStateOf(airports[0]) }
    var selectedDay by remember { mutableStateOf(days[0]) }
    var airportMenuExpanded by remember { mutableStateOf(false) }
    var dayMenuExpanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

        // Airport & Day dropdowns
        TwoDropdownsInline(
            airports = airports,
            days = days,
            selectedAirport = selectedAirport,
            onAirportSelected = { selectedAirport = it },
            selectedDay = selectedDay,
            onDaySelected = { selectedDay = it },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons to fetch flights
        Row {
            Button(onClick = {
                scope.launch {
                    error = null
                    try {
                        val fetched = FlightawareApi.getApiData(selectedAirport, "all")
                        viewModel.insertFlights(fetched)
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }) { Text("All Activity") }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                scope.launch {
                    error = null
                    try {
                        val fetched = FlightawareApi.getApiData(selectedAirport, "arrivals")
                        viewModel.insertFlights(fetched)
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }) { Text("Arrivals") }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                scope.launch {
                    error = null
                    try {
                        val fetched = FlightawareApi.getApiData(selectedAirport, "departures")
                        viewModel.insertFlights(fetched)
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }) { Text("Departures") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display flights from DB
        if (error != null) {
            Text("Error: $error", color = Color.Red)
        } else if (flights.isEmpty()) {
            Text("No flight data yet.")
        } else {
            val now = ZonedDateTime.now()
            val tomorrowMidnight = ZonedDateTime.now().plusDays(1)
                .toLocalDate().atStartOfDay(ZoneId.systemDefault())

            val sortedFlights = flights.sortedBy { it.timestamp }
            val rows = mutableListOf<List<String>>()
            var prev: ZonedDateTime? = null

            for (flight in sortedFlights) {
                val time = flight.timestamp.withZoneSameInstant(ZoneId.systemDefault())
                if (time < now) continue
                if (selectedDay == "Tomorrow" && time < tomorrowMidnight) continue

                val localTime = time.toLocalTime()
                val formattedLocalTime = localTime.toString().substring(0, 5)

                val diff = prev?.let {
                    val minutes = java.time.Duration.between(it, flight.timestamp).toMinutes().toString()
                    "$minutes min"
                } ?: ""

                if (diff.isNotEmpty()) {
                    rows.add(listOf("", "", "", "", diff))
                }

                val label = if (flight.operation == "arrivals") "Arr" else "Dep"

                rows.add(
                    listOf(
                        label,
                        formattedLocalTime,
                        flight.ident,
                        flight.aircraftType,
                        ""
                    )
                )
                prev = flight.timestamp
            }

            // LazyColumn with headings
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Op", "Time", "Flight", "Aircraft", "Until Next").forEach {
                            Text(text = it, modifier = Modifier.weight(1f), color = Color.Gray)
                        }
                    }
                }

                items(rows) { row ->
                    val bColor = when {
                        row[0] == "Arr" -> ArrivalRow
                        row[0] == "Dep" -> DepartureRow
                        row[4].isNotEmpty() -> WaitRow
                        else -> Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bColor)
                            .padding(vertical = 4.dp)
                    ) {
                        val tColor = when {
                            row[0] == "Arr" -> TextAction
                            row[0] == "Dep" -> TextAction
                            row[4].isNotEmpty() -> TextWait
                            else -> Color.Unspecified
                        }

                        row.forEach {
                            Text(text = it, modifier = Modifier.weight(1f), color = tColor)
                        }
                    }
                }
            }
        }
    }
}
