package com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal;

import android.media.audiofx.Equalizer;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.AppEqualizer;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class InternalEqualizer implements AppEqualizer {

    private Equalizer equalizer;

    @Override
    public void attachEqualizer(int audioSessionId) {
        if (audioSessionId != 0) {
            equalizer = new Equalizer(1000, audioSessionId);
            equalizer.setEnabled(true);

            System.out.println("KEK bands level range: " + Arrays.toString(equalizer.getBandLevelRange()));
            for(short i = 0; i < equalizer.getNumberOfBands(); i++) {
                System.out.println("KEK band i: " + i);
                System.out.println("KEK band freq range: " + Arrays.toString(equalizer.getBandFreqRange(i)));
                System.out.println("KEK band level: " + equalizer.getBandLevel(i));
            }
        }
    }

    @Override
    public void detachEqualizer(int audioSessionId) {
        if (equalizer != null) {
            equalizer.setEnabled(false);
        }
    }

    public Single<List<Band>> getBands() {
        return Single.fromCallable(() -> {
            Equalizer tempEqualizer = new Equalizer(0, 1);
            List<Band> bands = new ArrayList<>();
            for(short i = 0; i < tempEqualizer.getNumberOfBands(); i++) {
                bands.add(new Band(
                        i,
                        tempEqualizer.getBandFreqRange(i),
                        tempEqualizer.getBandLevelRange(),
                        tempEqualizer.getBandLevel(i))
                );
            }

            tempEqualizer.release();

            return bands;
        });
    }

    public void setBandLevel(short bandNumber, short level) {
        if (equalizer != null) {
            equalizer.setBandLevel(bandNumber, level);
        }
    }

}
