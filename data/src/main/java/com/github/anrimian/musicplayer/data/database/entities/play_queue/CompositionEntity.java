package com.github.anrimian.musicplayer.data.database.entities.play_queue;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import javax.annotation.Nullable;

@Entity(tableName = "compositions")
public class CompositionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    private String artist;
    private String title;
    private String album;
    private String filePath;

    private long duration;
    private long size;
    private long mediaStoreId;

    private Date dateAdded;
    private Date dateModified;

    private boolean isCorrupted;
}
