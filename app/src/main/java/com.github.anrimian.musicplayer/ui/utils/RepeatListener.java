package com.github.anrimian.musicplayer.ui.utils;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class RepeatListener implements OnTouchListener {

    private final static int TOUCH_OFFSET = 20;
    private final static int CALL_COUNT_TO_INCREASE_SPEED = 5;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final int initialInterval;
    private final int normalInterval;
    private final Runnable startListener;
    private final Runnable actionListener;

    private final Rect touchHoldRect = new Rect();

    private View touchedView;
    private boolean calledAtLeastOnce = false;

    private short callCount = 0;

    private final Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            if (touchedView.isEnabled()) {
                handler.postDelayed(this, getCallInterval());
                actionListener.run();
                callCount++;
                if (!calledAtLeastOnce && startListener != null) {
                    startListener.run();
                }
                calledAtLeastOnce = true;
            } else {
                stopRepeatCall();
                touchedView.setPressed(false);
                touchedView = null;
                calledAtLeastOnce = false;
            }
        }
    };

    public RepeatListener(int initialInterval,
                          int normalInterval,
                          Runnable startListener,
                          Runnable actionListener) {
        if (actionListener == null) {
            throw new IllegalArgumentException("null runnable");
        }
        if (initialInterval < 0 || normalInterval < 0) {
            throw new IllegalArgumentException("negative interval");
        }

        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
        this.startListener = startListener;
        this.actionListener = actionListener;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                stopRepeatCall();
                calledAtLeastOnce = false;
                handler.postDelayed(handlerRunnable, initialInterval);
                touchedView = view;
                touchHoldRect.set(view.getLeft() - TOUCH_OFFSET,
                        view.getTop() - TOUCH_OFFSET,
                        view.getRight() + TOUCH_OFFSET,
                        view.getBottom() + TOUCH_OFFSET);
                return false;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                stopRepeatCall();
                if (calledAtLeastOnce) {
                    touchedView.setPressed(false);
                }
                touchedView = null;
                boolean processed = calledAtLeastOnce;

                calledAtLeastOnce = false;
                return processed;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!touchHoldRect.contains(
                        view.getLeft() + (int) motionEvent.getX(),
                        view.getTop() + (int) motionEvent.getY())) {
                    stopRepeatCall();
                }
                break;
            }
        }

        return false;
    }

    private void stopRepeatCall() {
        handler.removeCallbacks(handlerRunnable);
        callCount = 0;
    }

    private long getCallInterval() {
        if (callCount <= CALL_COUNT_TO_INCREASE_SPEED) {
            return normalInterval;
        } else {
            return normalInterval / 2;
        }
    }

}
