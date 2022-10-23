package com.github.anrimian.musicplayer.data.models.composition;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(value = {
        AudioFileType.MUSIC,
        AudioFileType.PODCAST,
        AudioFileType.AUDIOBOOK,
        AudioFileType.RECORDING,
        AudioFileType.ALARM,
        AudioFileType.NOTIFICATION,
        AudioFileType.RINGTONE,
})
@Retention(RetentionPolicy.SOURCE)
public @interface AudioFileType {
    int MUSIC = 1;
    int PODCAST = 2;
    int AUDIOBOOK = 3;
    int RECORDING = 4;
    int ALARM = 5;
    int NOTIFICATION = 6;
    int RINGTONE = 7;
}
