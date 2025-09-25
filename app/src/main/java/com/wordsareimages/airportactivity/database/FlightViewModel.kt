package com.wordsareimages.airportactivity.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wordsareimages.airportactivity.models.FlightInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlightViewModel(private val repository: FlightRepository) : ViewModel() {
    val flights = repository.getAllFlights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertFlights(list: List<FlightInfo>) {
        viewModelScope.launch {
            repository.insertFlights(list)
        }
    }

    fun clearFlights() {
        viewModelScope.launch {
            repository.clearFlights()
        }
    }
}
