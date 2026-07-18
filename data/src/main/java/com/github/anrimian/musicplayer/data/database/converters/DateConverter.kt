package com.github.anrimian.musicplayer.data.database.converters;

import androidx.room.TypeConverter;

import java.util.Date;

public class DateConverter {

    @TypeConverter
    public Long toMillis(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }

    @TypeConverter
    public Date toDate(Long millis) {
        if (millis == null) {
            return null;
        } else {
            return new Date(millis);
        }
    }
}
