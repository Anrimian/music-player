package com.github.anrimian.simplemusicplayer.domain;

import java.util.stream.Stream;

import io.reactivex.Single;

/**
 * Created on 18.10.2017.
 */

public class TestInteractor {

    private TextRepository textRepository;

    public TestInteractor(TextRepository textRepository) {
        this.textRepository = textRepository;
    }

    public Single<String> getSomeData() {
        return textRepository.getSomeData().map(o -> o + "2");
    }
}
