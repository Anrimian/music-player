package com.github.anrimian.musicplayer.data.database.entities;

public class IdPair {

    private final long dbId;
    private final long storageId;

    public IdPair(long dbId, long storageId) {
        this.dbId = dbId;
        this.storageId = storageId;
    }

    public long getDbId() {
        return dbId;
    }

    public long getStorageId() {
        return storageId;
    }
}
