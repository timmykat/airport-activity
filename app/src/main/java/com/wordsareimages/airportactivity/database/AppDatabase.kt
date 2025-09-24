package com.wordsareimages.airportactivity.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.wordsareimages.airportactivity.models.FlightInfo
import com.wordsareimages.airportactivity.utilities.Converters

@Database(entities = [FlightInfo::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flightDao(): FlightDao
}