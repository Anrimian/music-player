package com.github.anrimian.musicplayer.ui.common.menu;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.appcompat.content.res.AppCompatResources;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.utils.ViewUtils;

import javax.annotation.Nullable;

@SuppressLint("RtlHardcoded")
public class AppPopupWindow {

    private static final long POPUP_OPEN_WINDOW_MILLIS = 200L;
    private static long lastOpenTime;

    @Nullable
    public static PopupWindow showPopupWindow(View anchorView,
                                              View popupView,
                                              int anchorGravity,
                                              int screenMargin) {
        return showPopupWindow(anchorView, popupView, anchorGravity, Gravity.NO_GRAVITY, screenMargin);
    }

    /**
        @param anchorGravity values: TOP, BOTTOM, START, END, CENTER
        @param gravity values: NO_GRAVITY, TOP, BOTTOM, CENTER_VERTICAL, START, END,
        CENTER_HORIZONTAL
     */
    @Nullable
    public static PopupWindow showPopupWindow(View anchorView,
                                              View popupView,
                                              int anchorGravity,
                                              int gravity,
                                              int screenMargin) {
        long currentTime = System.currentTimeMillis();
        if (lastOpenTime + POPUP_OPEN_WINDOW_MILLIS > currentTime) {
            return null;
        }
        lastOpenTime = currentTime;

        Context context = anchorView.getContext();


        //margins
        FrameLayout popupViewWrapper = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.setMargins(screenMargin, screenMargin, screenMargin, screenMargin);
        popupViewWrapper.addView(popupView, params);

        popupView.setElevation(5f);

        PopupWindow popupWindow = new PopupWindow(popupViewWrapper,
                WRAP_CONTENT,
                WRAP_CONTENT,
                true);

        popupWindow.setAnimationStyle(R.style.PopupAnimationStyle);
        //fix for closing by back button or touch on android 5.1
        popupWindow.setBackgroundDrawable(AppCompatResources.getDrawable(context, R.drawable.bg_transparent));
        //fix for closing by touch on android 12
        popupWindow.setOutsideTouchable(true);

        popupView.measure(
                makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );


        int[] location = new int[2];
        anchorView.getLocationInWindow(location);
        int anchorX = location[0];
        int anchorY = location[1];

        int anchorWidth = anchorView.getMeasuredWidth();
        int anchorHeight = anchorView.getMeasuredHeight();

        int viewWidth = popupView.getMeasuredWidth();
        int viewHeight = popupView.getMeasuredHeight();

        int showX = anchorX;
        int showY = anchorY;

        anchorGravity = normalizeGravity(anchorView, anchorGravity);
        gravity = normalizeGravity(anchorView, gravity);
        switch (anchorGravity) {
            case Gravity.TOP: {
                showY -= viewHeight - screenMargin;
                break;
            }
            case Gravity.BOTTOM: {
                showY += anchorHeight - screenMargin;
                break;
            }
            case Gravity.LEFT: {
                showX = showX - screenMargin - viewWidth;
                break;
            }
            case Gravity.RIGHT: {
                showX += anchorWidth;
                break;
            }
            case Gravity.CENTER: {
                showX = showX + anchorWidth - viewWidth;
            }
        }
        switch (gravity) {
            case Gravity.NO_GRAVITY: {
                switch (anchorGravity) {
                    case Gravity.LEFT:
                    case Gravity.RIGHT: {
                        showY -= screenMargin;
                        break;
                    }
                    case Gravity.TOP:
                    case Gravity.BOTTOM: {
                        showX -= screenMargin;
                        break;
                    }
                    case Gravity.CENTER: {
                        showY -= screenMargin;
                        showX -= screenMargin;
                    }
                }

                break;
            }
            case Gravity.TOP: {
                showY = showY + anchorHeight - viewHeight - screenMargin;
                break;
            }
            case Gravity.BOTTOM: {
                showY += anchorHeight;
                break;
            }
            case Gravity.CENTER_VERTICAL: {
                showY = showY + anchorHeight/2 - viewHeight/2  - screenMargin;
                break;
            }
            case Gravity.LEFT: {
                showX -= viewWidth;
                break;
            }
            case Gravity.RIGHT: {
                showX += viewWidth;
                break;
            }
            case Gravity.CENTER_HORIZONTAL: {
                showX = showX + anchorWidth/2 - viewWidth/2;
            }
        }

        View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {}

            @Override
            public void onViewDetachedFromWindow(View view) {
                popupWindow.dismiss();
            }
        };
        anchorView.addOnAttachStateChangeListener(listener);
        popupWindow.setOnDismissListener(() ->
                anchorView.removeOnAttachStateChangeListener(listener)
        );

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, showX, showY);

        return popupWindow;
    }

    private static int normalizeGravity(View view, int gravity) {
        boolean isRtl = ViewUtils.isRtl(view);
        if (gravity == Gravity.START) {
            if (isRtl) {
                return Gravity.RIGHT;
            } else {
                return Gravity.LEFT;
            }
        }
        if (gravity == Gravity.END) {
            if (isRtl) {
                return Gravity.LEFT;
            } else {
                return Gravity.RIGHT;
            }
        }
        return gravity;
    }

}
