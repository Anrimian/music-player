package com.github.anrimian.musicplayer.data.storage.providers.music;

import android.app.PendingIntent;

public class RecoverableSecurityExceptionExt extends SecurityException {

    private final PendingIntent pIntent;

    public RecoverableSecurityExceptionExt(PendingIntent pIntent, String message) {
        super(message);
        this.pIntent = pIntent;
    }

    public PendingIntent getPIntent() {
        return pIntent;
    }
}
