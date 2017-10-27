package com.github.anrimian.simplemusicplayer.domain.business.music.utils;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;

import java.util.List;

import io.reactivex.Single;

/**
 * Created on 26.10.2017.
 */

public class FakeMusicProviderRepository implements MusicProviderRepository {

    private List<Composition> fakeCompositions;

    public FakeMusicProviderRepository(List<Composition> fakeCompositions) {
        this.fakeCompositions = fakeCompositions;
    }

    @Override
    public Single<List<Composition>> getAllCompositions() {
        return Single.just(fakeCompositions);
    }
}
