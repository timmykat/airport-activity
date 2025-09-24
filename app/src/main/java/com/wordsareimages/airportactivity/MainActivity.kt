package com.wordsareimages.airportactivity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.wordsareimages.airportactivity.flightaware.FlightawareApi
import com.wordsareimages.airportactivity.models.FlightInfo
import com.wordsareimages.airportactivity.ui.theme.AirportActivityTheme
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.wordsareimages.airportactivity.ui.theme.ArrivalRow
import com.wordsareimages.airportactivity.ui.theme.DepartureRow
import com.wordsareimages.airportactivity.ui.theme.TextAction
import com.wordsareimages.airportactivity.ui.theme.TextWait
import com.wordsareimages.airportactivity.ui.theme.WaitRow
import java.time.ZoneId


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AirportFlightActivityApp
        val factory = FlightViewModelFactory(app.repository)

        setContent {
            AirportActivityTheme {
                // Provide the ViewModel to Compose
                val viewModel: FlightViewModel = viewModel(factory = factory)
                AirportActivityApp(viewModel)
            }
        }
    }
}

@Composable
fun TwoDropdownsInline(
    airports: List<String>,
    days: List<String>
) {
    var selectedAirport by remember { mutableStateOf(airports.firstOrNull() ?: "") }
    var selectedDay by remember { mutableStateOf(days.firstOrNull() ?: "") }

    var airportMenuExpanded by remember { mutableStateOf(false) }
    var dayMenuExpanded by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth()) {

        // Airport Dropdown
        Box(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .clickable { airportMenuExpanded = true }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = selectedAirport)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Airport Dropdown Arrow"
                )
            }

            DropdownMenu(
                expanded = airportMenuExpanded,
                onDismissRequest = { airportMenuExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                airports.forEach { airport ->
                    DropdownMenuItem(
                        text = { Text(airport) },
                        onClick = {
                            selectedAirport = airport
                            airportMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp)) // space between dropdowns

        // Day Dropdown
        Box(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .clickable { dayMenuExpanded = true }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = selectedDay)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Day Dropdown Arrow"
                )
            }

            DropdownMenu(
                expanded = dayMenuExpanded,
                onDismissRequest = { dayMenuExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                days.forEach { day ->
                    DropdownMenuItem(
                        text = { Text(day) },
                        onClick = {
                            selectedDay = day
                            dayMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun AirportActivityApp() {
    val airports = listOf("KPVD", "KBOS")
    var selectedAirport by remember { mutableStateOf(airports[0]) }
    val days = listOf("Today", "Tomorrow")
    var selectedDay by remember { mutableStateOf(days[0]) }
    var airportMenuExpanded by remember { mutableStateOf(false) }
    var dayMenuExpanded by remember { mutableStateOf(false) }
    var flights by remember { mutableStateOf<List<FlightInfo>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth()) {

        Spacer(modifier = Modifier.height(30.dp)) // space between dropdowns

        TwoDropdownsInline(airports, days)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    scope.launch {
                        error = null
                        try {
                            flights = FlightawareApi.getApiData(selectedAirport, "all")
                        } catch (e: Exception) {
                            error = e.message
                            flights = emptyList()
                        }
                    }
                }
            ) {
                Text("All Activity")
            }

            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        error = null
                        try {
                            flights = FlightawareApi.getApiData(selectedAirport, "arrivals")
                        } catch (e: Exception) {
                            error = e.message
                            flights = emptyList()
                        }
                    }
                }
            ) {
                Text("Arrivals")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        error = null
                        try {
                            flights = FlightawareApi.getApiData(selectedAirport, "departures")
                        } catch (e: Exception) {
                            error = e.message
                            flights = emptyList()
                        }
                    }
                }
            ) {
                Text("Departures")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (flights.isNotEmpty()) {
            val now = ZonedDateTime.now()
            val tomorrowMidnight = ZonedDateTime.now()
                .plusDays(1)
                .toLocalDate()
                .atStartOfDay(ZoneId.systemDefault());
            val sortedFlights = flights.sortedBy { it.timestamp }
            val rows = mutableListOf<List<String>>()
            var prev: ZonedDateTime? = null

                for (flight in sortedFlights) {
                    val time = flight.timestamp.withZoneSameInstant(ZoneId.systemDefault())
                    if (time < now) {
                        continue
                    } else if (selectedDay == "Tomorrow" && time < tomorrowMidnight) {
                        continue
                    }
                    val localTime = time.toLocalTime()
                    val formattedLocalTime = localTime.toString().substring(0, 5) // "HH:mm"
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

            // Headings
            LazyColumn(modifier = Modifier.fillMaxWidth()
                .weight(1f)) {
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("Op", "Time", "Flight", "Aircraft", "Until Next").forEach {
                            Text(
                                text = it,
                                modifier = Modifier.weight(1f),
                                color = Color.Gray
                            )
                        }
                    }
                }

                items(rows) { row ->
                    val bColor = when {
                        row[0] == "Arr" || row[0] == "arrival" -> ArrivalRow
                        row[0] == "Dep" || row[0] == "departure" -> DepartureRow
                        row[4].isNotEmpty() -> WaitRow // Blue for "until next"
                        else -> Color.Transparent
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .background(bColor)
                        .padding(vertical = 4.dp)) {

                        val tColor = when {
                            row[0] == "Arr" || row[0] == "arrival" -> TextAction // Green
                            row[0] == "Dep" || row[0] == "departure" -> TextAction // Red
                            row[4].isNotEmpty() -> TextWait // Blue for "until next"
                            else -> Color.Unspecified
                        }

                        row.forEach {
                            Text(text = it, modifier = Modifier.weight(1f), color = tColor)
                        }
                    }
                }
            }
        } else if (error != null) {
            Text("Error: $error", color = Color.Red)
        } else {
            Text("No flight data yet.")
        }
    }
}