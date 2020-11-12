package com.github.anrimian.musicplayer.ui.utils;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

//often click doesn't work - fixed
//move out of view bounds

//increasing rewind speed
//save positions after rewind
//add vibration after rewind starts
//implement rewind from service
//implement rewind in external player
//remove skip to next button disabling behavior
public class RepeatListener implements OnTouchListener {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final int initialInterval;
    private final int normalInterval;
    private final Runnable actionListener;

    private View touchedView;
    private boolean calledAtLeastOnce = false;

    private final Rect viewRect = new Rect();

    private final Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            if (touchedView.isEnabled()) {
                handler.postDelayed(this, normalInterval);
                actionListener.run();
                calledAtLeastOnce = true;
            } else {
                // if the view was disabled by the clickListener, remove the callback
                handler.removeCallbacks(handlerRunnable);
                touchedView.setPressed(false);
                touchedView = null;
                calledAtLeastOnce = false;
            }
        }
    };

    public RepeatListener(int initialInterval,
                          int normalInterval,
                          Runnable actionListener) {
        if (actionListener == null) {
            throw new IllegalArgumentException("null runnable");
        }
        if (initialInterval < 0 || normalInterval < 0) {
            throw new IllegalArgumentException("negative interval");
        }

        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
        this.actionListener = actionListener;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                handler.removeCallbacks(handlerRunnable);
                calledAtLeastOnce = false;
                handler.postDelayed(handlerRunnable, initialInterval);
                touchedView = view;
                viewRect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                return false;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                handler.removeCallbacks(handlerRunnable);
                if (calledAtLeastOnce) {
                    touchedView.setPressed(false);
                }
                touchedView = null;
                boolean processed = calledAtLeastOnce;

                calledAtLeastOnce = false;
                return processed;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!viewRect.contains(view.getLeft() + (int) motionEvent.getX(),
                        view.getTop() + (int) motionEvent.getY())) {
//                    resetTouch();
                }
                break;
            }
        }

        return false;
    }

}
