package com.github.anrimian.simplemusicplayer.domain;

import java.util.stream.Stream;

import io.reactivex.Single;

/**
 * Created on 18.10.2017.
 */

public interface TextRepository {

    Single<String> getSomeData();
}
