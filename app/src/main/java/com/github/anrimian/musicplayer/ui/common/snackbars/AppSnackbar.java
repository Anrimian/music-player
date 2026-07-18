package com.github.anrimian.musicplayer.ui.common.snackbars;

import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.utils.views.progress_bar.ProgressBarCountDownTimer;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

public class AppSnackbar extends BaseTransientBottomBar<AppSnackbar>  {

    private static final int DURATION_SHORT_MILLIS = 2000;
    private static final int DURATION_LONG_MILLIS = 5000;
    private static final int DURATION_INDEFINITE_MILLIS = Integer.MAX_VALUE;

    private final TextView tvMessage;
    private final Button tvAction;
    private final ProgressBar progressBar;

    private boolean launchCountDownTimer = false;
    private long durationMillis;

    private CountDownTimer countDownTimer;

    public static AppSnackbar make(ViewGroup parent, String text) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.snackbar_view, parent, false);

        ContentViewCallback callback = new ContentViewCallback();
        AppSnackbar appSnackbar = new AppSnackbar(parent, view, callback, text);
        appSnackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        View snackbarView = appSnackbar.getView();
        int padding = parent.getResources().getDimensionPixelSize(R.dimen.snackbar_margin);
        snackbarView.setPadding(padding, padding, padding, padding);
        snackbarView.setBackgroundColor(Color.TRANSPARENT);

        return appSnackbar;
    }

    protected AppSnackbar(@NonNull ViewGroup parent,
                          @NonNull View content,
                          @NonNull ContentViewCallback contentViewCallback,
                          String message) {
        super(parent, content, contentViewCallback);
        tvMessage = content.findViewById(R.id.tv_message);
        tvMessage.setText(message);

        tvAction = content.findViewById(R.id.tv_action);
        tvAction.setVisibility(View.GONE);

        progressBar = content.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        setDurationMillis(DURATION_SHORT_MILLIS);
    }

    @NonNull
    @Override
    public Behavior getBehavior() {
        return new Behavior() {
            @Override
            public boolean onTouchEvent(
                    @NonNull CoordinatorLayout parent,
                    @NonNull View child,
                    @NonNull MotionEvent event
            ) {
                try {
                    return super.onTouchEvent(parent, child, event);
                } catch (IllegalArgumentException e) {
                    // ViewDragHelper.captureChildView throws when the snackbar view
                    // is detached from CoordinatorLayout during active touch processing.
                    // This is a known framework race condition — suppress gracefully.
                    return false;
                }
            }
        };
    }

    public AppSnackbar setText(CharSequence text) {
        tvMessage.setText(text);
        return this;
    }

    public AppSnackbar setAction(@StringRes int actionText, Runnable listener) {
        return setAction(getContext().getString(actionText), listener);
    }

    public AppSnackbar setAction(CharSequence text, Runnable listener) {
        tvAction.setText(text);
        tvAction.setVisibility(View.VISIBLE);
        tvAction.setOnClickListener(view -> {
            listener.run();
            dismiss();
        });
        launchCountDownTimer = true;
        return this;
    }

    public AppSnackbar duration(@Snackbar.Duration int duration) {
        int durationMillis = DURATION_SHORT_MILLIS;
        switch (duration) {
            case Snackbar.LENGTH_LONG: {
                durationMillis = DURATION_LONG_MILLIS;
                break;
            }
            case Snackbar.LENGTH_SHORT: {
                durationMillis = DURATION_SHORT_MILLIS;
                break;
            }
            case Snackbar.LENGTH_INDEFINITE: {
                durationMillis = DURATION_INDEFINITE_MILLIS;
                break;
            }
        }
        return setDurationMillis(durationMillis);
    }

    public AppSnackbar setDurationMillis(int durationMillis) {
        this.durationMillis = durationMillis;
        setDuration(durationMillis);
        return this;
    }

    @Override
    public void show() {
        super.show();
        if (launchCountDownTimer && durationMillis < DURATION_INDEFINITE_MILLIS) {
            progressBar.setVisibility(View.VISIBLE);
            long totalTimeMs = durationMillis;
            countDownTimer = new ProgressBarCountDownTimer(totalTimeMs, 50, progressBar);
            countDownTimer.start();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private static class ContentViewCallback implements com.google.android.material.snackbar.ContentViewCallback {

        @Override
        public void animateContentIn(int delay, int duration) {}

        @Override
        public void animateContentOut(int delay, int duration) {}
    }

}
