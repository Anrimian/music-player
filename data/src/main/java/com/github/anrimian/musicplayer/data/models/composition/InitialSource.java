package com.github.anrimian.musicplayer.data.models.composition;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef(value = {
        InitialSource.LOCAL,
        InitialSource.REMOTE
})
@Retention(RetentionPolicy.SOURCE)
@Deprecated
public @interface InitialSource {
    int LOCAL = 1;
    int REMOTE = 2;
}
