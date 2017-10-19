package com.github.anrimian.simplemusicplayer.data;

import com.github.anrimian.simplemusicplayer.domain.TextRepository;

import io.reactivex.Single;

/**
 * Created on 18.10.2017.
 */

public class TestRepositoryImpl implements TextRepository {

    @Override
    public Single<String> getSomeData() {
        return Single.just("hey").map(o -> o + 1);
    }
}
