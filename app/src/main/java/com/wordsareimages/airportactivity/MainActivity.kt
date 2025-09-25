package com.wordsareimages.airportactivity

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
import androidx.lifecycle.viewmodel.compose.viewModel

import com.wordsareimages.airportactivity.flightaware.FlightawareApi
import com.wordsareimages.airportactivity.models.FlightInfo
import com.wordsareimages.airportactivity.ui.theme.AirportActivityTheme
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.wordsareimages.airportactivity.database.FlightViewModel
import com.wordsareimages.airportactivity.ui.AirportActivityApp
import com.wordsareimages.airportactivity.ui.FlightViewModelFactory
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
