package com.github.anrimian.musicplayer.data.database.entities.playlist;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;

import javax.annotation.Nullable;

@Entity(tableName = "play_lists_entries",
        foreignKeys = {
                @ForeignKey(entity = CompositionEntity.class,
                        parentColumns = {"id"},
                        childColumns = {"audioId"},
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = PlayListEntity.class,
                        parentColumns = {"id"},
                        childColumns = {"playListId"},
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index({"audioId"}),
                @Index({"playListId"})
        }
)
public class PlayListEntryEntity {

    @PrimaryKey(autoGenerate = true)
    private long itemId;

    @Nullable
    private Long storageItemId;

    private long audioId;
    private long playListId;

    private int orderPosition;

    public PlayListEntryEntity(@Nullable Long storageItemId,
                               long audioId,
                               long playListId,
                               int orderPosition) {
        this.storageItemId = storageItemId;
        this.audioId = audioId;
        this.playListId = playListId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getItemId() {
        return itemId;
    }

    @Nullable
    public Long getStorageItemId() {
        return storageItemId;
    }

    public long getAudioId() {
        return audioId;
    }

    public long getPlayListId() {
        return playListId;
    }

    public int getOrderPosition() {
        return orderPosition;
    }
}
