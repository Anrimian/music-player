package com.github.anrimian.musicplayer.data.database.converters;

import androidx.room.TypeConverter;

import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;

import javax.annotation.Nullable;

public class EnumConverter {

    @TypeConverter
    public CorruptionType toEnum(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return CorruptionType.valueOf(name);
    }

    @TypeConverter
    public String toName(@Nullable CorruptionType value) {
        if (value == null) {
            return null;
        }
        return value.name();
    }
}
