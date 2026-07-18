package com.github.anrimian.musicplayer.data.database.converters

import androidx.room.TypeConverter
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource.Companion.fromId
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus

class EnumConverter {

    @TypeConverter
    fun toLocalFileStatus(id: Int): LocalFileStatus {
        return LocalFileStatus.fromId(id)
    }

    @TypeConverter
    fun toId(value: LocalFileStatus): Int {
        return value.id
    }

    @TypeConverter
    fun toEnum(id: Int?): CorruptionType? {
        if (id == null) {
            return null
        }
        return CorruptionType.fromId(id)
    }

    @TypeConverter
    fun toId(value: CorruptionType?): Int? {
        if (value == null) {
            return null
        }
        return value.id
    }

    @TypeConverter
    fun toInitialSource(value: Int): InitialSource {
        return fromId(value)
    }

    @TypeConverter
    fun toInt(value: InitialSource): Int {
        return value.id
    }

}
