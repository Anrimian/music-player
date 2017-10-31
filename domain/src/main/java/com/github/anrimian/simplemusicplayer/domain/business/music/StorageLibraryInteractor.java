package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.files.FileSource;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public interface StorageLibraryInteractor {

    Single<List<FileSource>> getMusicInPath(@Nullable String path);

    void playAllMusicInPath(@Nullable String path);

    void playMusic(Composition composition);
}
