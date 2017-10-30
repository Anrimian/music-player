package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.FileTree;

import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public interface StorageLibraryInteractor {

    Single<FileTree<Composition>> getAllMusicInPath(@Nullable String path);

    void playAllMusicInPath(@Nullable String path);

    Single<List<MusicFileSource>> getMusicInPath(@Nullable String path);

    void playMusic(MusicFileSource musicFileSource);
}
