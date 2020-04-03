package com.github.anrimian.musicplayer.ui.common.menu;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.github.anrimian.musicplayer.R;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class AppPopupMenu {

    //override overflow button click
    //screen offset:
    // top, - horizontal orientation - doesn't fit(bottom offset breaks)
    // start,
    // end
    public static PopupWindow showPopupWindow(Activity activity, View anchorView, View popupView) {
        Context context = anchorView.getContext();
        Resources resources = context.getResources();

        PopupWindow popupWindow = new PopupWindow(popupView,
                WRAP_CONTENT,
                WRAP_CONTENT,
                true);
        popupWindow.setElevation(6f);

        popupView.measure(
                makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );


        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int anchorX = location[0];
        int anchorY = location[1];

        int anchorWidth = anchorView.getMeasuredWidth();
        int anchorHeight = anchorView.getMeasuredHeight();

        int viewWidth = popupView.getMeasuredWidth();
        int viewHeight = popupView.getMeasuredHeight();

        int yTouchOffset = resources.getDimensionPixelSize(R.dimen.popup_y_touch_offset);
        int screenMargin = resources.getDimensionPixelSize(R.dimen.popup_screen_margin);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;

        int xOff = viewWidth * -1;

        //move window to top to fit it
        int viewBottomY = anchorY + viewHeight;
        int viewNotFitHeight = viewBottomY - screenHeight;
        if (viewNotFitHeight < 0) {
            viewNotFitHeight = 0;
        }
        int yOffsetAdditional = Math.max(yTouchOffset, viewNotFitHeight);
        int yOff = (anchorHeight + yOffsetAdditional) * -1;

        //add screen margin at bottom
        int bottomOffset = (viewBottomY + yOff - screenHeight);
        if (bottomOffset < 0) {
            bottomOffset = 0;
        }
        if (bottomOffset < screenMargin) {
            yOff -= screenMargin - bottomOffset;
        }

        //add screen margin at top
        int viewTopY = anchorY + yOff;
        if (viewTopY < 0) {
            viewTopY = 0;
        }
        if (viewTopY < screenMargin) {
            yOff += screenMargin - viewTopY;
        }

        //cut at bottom if necessary
        int bottomYPos = viewBottomY + yOff;
        int deltaBottomPos = bottomYPos - screenHeight;
        if (deltaBottomPos > 0) {
            ViewGroup.LayoutParams params = popupView.getLayoutParams();
            params.height = viewHeight - deltaBottomPos - screenMargin;
            popupView.setLayoutParams(params);
        }

        popupWindow.showAsDropDown(anchorView, xOff, yOff, Gravity.START | Gravity.TOP);

        return popupWindow;
    }

}
