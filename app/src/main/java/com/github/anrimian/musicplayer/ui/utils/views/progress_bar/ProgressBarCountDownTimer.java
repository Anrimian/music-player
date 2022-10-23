package com.github.anrimian.musicplayer.ui.utils.views.progress_bar;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.setProgress;

import android.os.CountDownTimer;
import android.widget.ProgressBar;

public class ProgressBarCountDownTimer extends CountDownTimer {

    private final long totalTimeMs;
    private final ProgressBar progressBar;

    public ProgressBarCountDownTimer(long millisInFuture,
                                     long countDownInterval,
                                     ProgressBar progressBar) {
        super(millisInFuture, countDownInterval);
        this.totalTimeMs = millisInFuture;
        this.progressBar = progressBar;
        this.progressBar.setProgress(100);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        int progress = (int) (millisUntilFinished * 100 / totalTimeMs);
        setProgress(progressBar, progress);
    }

    @Override
    public void onFinish() {
        setProgress(progressBar, 0);
    }
}
