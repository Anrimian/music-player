package com.github.anrimian.musicplayer.data.database.converters

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {

    @TypeConverter
    fun toMillis(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(millis: Long?): Date? = if (millis == null) { null } else { Date(millis) }

}
