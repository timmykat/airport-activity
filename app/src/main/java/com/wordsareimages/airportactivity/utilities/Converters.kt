package com.wordsareimages.airportactivity.utilities

import androidx.room.TypeConverter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): ZonedDateTime? {
        return value?.let { Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")) }
    }

    @TypeConverter
    fun dateToTimestamp(date: ZonedDateTime?): Long? {
        return date?.toInstant()?.toEpochMilli()
    }
}
