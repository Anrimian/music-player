package com.github.anrimian.musicplayer.domain.interactors.player;

public class SleepTimerInteractor {

    public void start() {
        //set enabled state
        //start timer if present
    }

    public void stop() {
        //set disabled state
        //cancel timer
    }

    public void onCompositionPlayFinished() {
        //if enabled
        //decrease counter
        //stop if counter is 0 OR flag to stop is enabled
    }

    public void setFinishTime() {
        //save finish time
    }

    public void setCompositionToFinishCount() {
        //save count
    }

    public void setPlayLastSong() {
        //save flat
    }

    private void onTimerFinished() {
        //if finish play last song is enabled -> set flag to stop
        //else stop
    }
}
