package com.github.anrimian.musicplayer.data.database.entities.genres;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;

import javax.annotation.Nullable;

@Entity(tableName = "genre_entries",
        foreignKeys = {
                @ForeignKey(entity = CompositionEntity.class,
                        parentColumns = "id",
                        childColumns = "audioId",
                        onDelete = ForeignKey.CASCADE)
        }
)
public class GenreEntryEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long audioId;

    @Nullable
    private Long storageId;

    public GenreEntryEntity(long audioId, @Nullable Long storageId) {
        this.audioId = audioId;
        this.storageId = storageId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAudioId() {
        return audioId;
    }

    public void setAudioId(long audioId) {
        this.audioId = audioId;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    public void setStorageId(@Nullable Long storageId) {
        this.storageId = storageId;
    }
}
