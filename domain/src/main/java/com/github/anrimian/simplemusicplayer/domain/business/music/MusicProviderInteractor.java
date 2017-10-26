package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.FileTree;

import javax.annotation.Nullable;

import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public interface MusicProviderInteractor {

    Single<FileTree<Composition>> getAllMusicInPath(@Nullable String path);
}
