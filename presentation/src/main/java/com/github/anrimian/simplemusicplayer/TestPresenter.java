package com.github.anrimian.simplemusicplayer;

import com.github.anrimian.simplemusicplayer.domain.TestInteractor;
import com.github.anrimian.simplemusicplayer.domain.TextRepository;

/**
 * Created on 18.10.2017.
 */

public class TestPresenter {

    public TestPresenter(TestInteractor testInteractor) {
        testInteractor.getSomeData().subscribe(this::onSomeDataLoaded);
    }

    private void onSomeDataLoaded(String data) {
        System.out.println(data);
    }
}
