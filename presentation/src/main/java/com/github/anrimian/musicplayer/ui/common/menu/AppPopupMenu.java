package com.github.anrimian.musicplayer.ui.common.menu;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
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
    //screen offset
    //window doesn't fit into screen case
    public static PopupWindow showPopupWindow(View anchorView, View popupView) {
        Context context = anchorView.getContext();
        Resources resources = context.getResources();

        PopupWindow popupWindow = new PopupWindow(popupView,
                WRAP_CONTENT,
                WRAP_CONTENT,
                true);
        popupWindow.setElevation(6f);
        popupView.setOnClickListener(v -> {
            Toast.makeText(context, "Wow, popup action button", Toast.LENGTH_SHORT).show();
        });

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

        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int screenHeight = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        int yTouchOffset = resources.getDimensionPixelSize(R.dimen.popup_y_touch_offset);
        int screenMargin = resources.getDimensionPixelSize(R.dimen.popup_screen_margin);

        int xOff = viewWidth * -1;

        int viewNotFitHeight = (anchorY + viewHeight) - screenHeight;
        if (viewNotFitHeight < 0) {
            viewNotFitHeight = 0;
        }

        int yOff = (anchorView.getMeasuredHeight() + yTouchOffset + viewNotFitHeight) * -1;

        //cut at bottom
        int bottomYPos = anchorY + yOff + viewHeight;
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
