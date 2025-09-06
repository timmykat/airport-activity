package com.example.airportactivity

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.airportactivity.flightaware.FlightawareApi
import com.example.airportactivity.models.FlightInfo
import com.example.airportactivity.ui.theme.AirportActivityTheme
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.airportactivity.models.FlightCacheEntry
import java.time.ZoneId


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AirportActivityTheme {
                AirportActivityApp()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AirportActivityApp() {
    val airports = listOf("KPVD", "KBOS")
    var selectedAirport by remember { mutableStateOf(airports[0]) }
    var expanded by remember { mutableStateOf(false) }
    var flights by remember { mutableStateOf<List<FlightInfo>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    val flightCache = remember { mutableStateOf(mutableMapOf<String, FlightCacheEntry>()) }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(text = "Select Airport:")

        // Dropdown Menu (Spinner equivalent)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .clickable { expanded = true }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = selectedAirport)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown arrow"
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                airports.forEach { airport ->
                    DropdownMenuItem(
                        text = { Text(airport) },
                        onClick = {
                            selectedAirport = airport
                            expanded = false
                        }
                    )
                }
            }
        }

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
            val sortedFlights = flights.sortedBy { it.timestamp }
            val rows = mutableListOf<List<String>>()
            var prev: ZonedDateTime? = null

                for (flight in sortedFlights) {
                    val localTime = flight.timestamp.withZoneSameInstant(ZoneId.systemDefault()).toLocalTime().toString().substring(0, 5) // "HH:mm"
                    val diff = prev?.let {
                        val minutes = java.time.Duration.between(it, flight.timestamp).toMinutes().toString()
                        "$minutes min"
                    } ?: ""

                    if (diff.isNotEmpty()) {
                        rows.add(listOf("", "", "", "", diff))
                    }

                    rows.add(
                        listOf(
                            flight.operation,
                            localTime,
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
                        row[0] == "Arr" || row[0] == "arrival" -> Color(0xFF7EE5CE) // Green
                        row[0] == "Dep" || row[0] == "departure" -> Color(0xFF8AC1EA) // Red
                        row[4].isNotEmpty() -> Color(0xFF163450) // Blue for "until next"
                        else -> Color.Transparent
                    }

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .background(bColor)
                        .padding(vertical = 4.dp)) {

                        val tColor = when {
                            row[0] == "Arr" || row[0] == "arrival" -> Color(0xFF000000) // Green
                            row[0] == "Dep" || row[0] == "departure" -> Color(0xFF000000) // Red
                            row[4].isNotEmpty() -> Color(0xFFB6B4B4) // Blue for "until next"
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