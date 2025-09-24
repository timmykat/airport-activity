package com.wordsareimages.airportactivity

import android.app.Application
import androidx.room.Room
import com.wordsareimages.airportactivity.database.AppDatabase
import com.wordsareimages.airportactivity.database.FlightRepository

class AirportFlightActivityApp : Application() {
    val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "flights.db"
        ).build()
    }

    val repository by lazy { FlightRepository(database.flightDao()) }
}