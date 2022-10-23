package com.github.anrimian.musicplayer.ui.common.menu;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import androidx.appcompat.content.res.AppCompatResources;

import com.github.anrimian.musicplayer.R;

import javax.annotation.Nullable;

public class AppPopupWindow {

    private static final long POPUP_OPEN_WINDOW_MILLIS = 200L;
    private static long lastOpenTime;

    @Nullable
    public static PopupWindow showPopupWindow(View anchorView,
                                              View popupView,
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

        int showX = anchorX - viewWidth - screenMargin;
        int showY = anchorY - screenMargin;

        switch (gravity) {
            case Gravity.CENTER: {
                showX += anchorWidth;
                break;
            }
            case Gravity.BOTTOM: {
                showX += viewWidth;
                showY += anchorHeight;
                break;
            }
            case Gravity.END: {
                showX += anchorWidth + viewWidth + screenMargin;
                break;
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

        popupWindow.showAtLocation(anchorView, Gravity.START | Gravity.TOP, showX, showY);

        return popupWindow;
    }

}
