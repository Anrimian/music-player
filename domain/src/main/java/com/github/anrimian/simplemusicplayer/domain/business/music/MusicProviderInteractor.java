package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;

import javax.annotation.Nullable;

import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public interface MusicProviderInteractor {

    Single<MusicFileSource> getAllMusicInPath(@Nullable String path);
}
