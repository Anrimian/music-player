package com.github.anrimian.musicplayer.ui.common.menu;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

import com.github.anrimian.musicplayer.R;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class AppPopupMenu {

    //override overflow button click
    //screen offset:
    // top, - horizontal orientation - doesn't fit(bottom offset breaks)
    // start,
    // end
    public static PopupWindow showPopupWindow(Activity activity,
                                              View anchorView,
                                              View popupView,
                                              boolean coverAnchor) {
        Context context = anchorView.getContext();
        Resources resources = context.getResources();

        int screenMargin = resources.getDimensionPixelSize(R.dimen.popup_screen_margin);

        //margins
        FrameLayout popupViewWrapper = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.setMargins(screenMargin, screenMargin, screenMargin, screenMargin);
        popupViewWrapper.addView(popupView, params);

        PopupWindow popupWindow = new PopupWindow(popupViewWrapper,
                WRAP_CONTENT,
                WRAP_CONTENT,
                true);
        popupWindow.setElevation(4f);
        popupWindow.setAnimationStyle(R.style.PopupAnimationStyle);

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

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;

        int showX = anchorX - viewWidth + (coverAnchor? anchorWidth: 0) - screenMargin;
        int showY = anchorY - screenMargin;

        popupWindow.showAtLocation(anchorView, Gravity.START | Gravity.TOP, showX, showY);

        return popupWindow;
    }

}
