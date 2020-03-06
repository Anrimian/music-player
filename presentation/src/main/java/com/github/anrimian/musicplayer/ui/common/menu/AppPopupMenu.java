package com.github.anrimian.musicplayer.ui.common.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.github.anrimian.musicplayer.R;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class AppPopupMenu {

    //calculate show location
    //to override overflow button click
    //draw menu
    public static void showPopupWindow(View anchorView) {
        Context context = anchorView.getContext();
        Resources resources = context.getResources();

        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams")
        View popupView = inflater.inflate(R.layout.menu_popup, null);

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

        int yTouchOffset = 0;//resources.getDimensionPixelSize(R.dimen.popup_y_touch_offset);

        int xOff = viewWidth * -1;
        int yOff = (anchorView.getMeasuredHeight() + yTouchOffset) * -1;
        popupWindow.showAsDropDown(anchorView, xOff, yOff, Gravity.START | Gravity.TOP);

        //cut at bottom
        int bottomPadding = resources.getDimensionPixelSize(R.dimen.popup_y_bottom_padding);
        int bottomYPos = anchorY + yOff + viewHeight;
        int deltaBottomPos = bottomYPos - screenHeight;
        if (deltaBottomPos > 0) {
            ViewGroup.LayoutParams params = popupView.getLayoutParams();
            params.height = viewHeight - deltaBottomPos - bottomPadding;
            popupView.setLayoutParams(params);
        }
    }

}
