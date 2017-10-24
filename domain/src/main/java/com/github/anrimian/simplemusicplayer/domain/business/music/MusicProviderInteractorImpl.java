package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.utils.Tree;

import javax.annotation.Nullable;

import io.reactivex.Single;

/**
 * Created on 24.10.2017.
 */

public class MusicProviderInteractorImpl implements MusicProviderInteractor {

    private Tree<MusicFileSource> musicFileThree;

    private MusicProviderRepository musicProviderRepository;

    public MusicProviderInteractorImpl(MusicProviderRepository musicProviderRepository) {
        this.musicProviderRepository = musicProviderRepository;
    }

    @Override
    public Single<MusicFileSource> getAllMusicInPath(@Nullable String path) {
        return null;
    }

    private Single<Tree<MusicFileSource>> getMusicFileTree() {
        if (musicFileThree == null) {
            return createMusicFileTree();
        } else {
            return Single.just(musicFileThree);
        }
    }

    private Single<Tree<MusicFileSource>> createMusicFileTree() {
        return musicProviderRepository.getAllCompositions()
                .map(compositions -> {
                    musicFileThree = new Tree<>(new MusicFileSource());
                    Tree<MusicFileSource> current = musicFileThree;

                    for (Composition composition: compositions) {
                        Tree<MusicFileSource> root = current;

                        String filePath = composition.getFilePath();
                        for (String data: filePath.split("/")) {
//                            current = current.child(data);//TODO create file tree
                        }

                        current = root;
                    }
                    return musicFileThree;
                });
    }
}
