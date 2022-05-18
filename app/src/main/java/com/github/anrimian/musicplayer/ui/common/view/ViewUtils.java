package com.github.anrimian.musicplayer.ui.common.view;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.RepeatListener;

public class ViewUtils {

    private static final int REWIND_HOLD_START_MILLIS = 500;
    private static final int REWIND_HOLD_INTERVAL_MILLIS = 400;

    public static void setOnHoldListener(View view, Runnable runnable) {
        if (!view.hasOnClickListeners()) {
            view.setClickable(true);
        }
        view.setOnTouchListener(new RepeatListener(
                REWIND_HOLD_START_MILLIS,
                REWIND_HOLD_INTERVAL_MILLIS,
                () -> AndroidUtils.playShortVibration(view.getContext()),
                runnable));
    }

    public static void onLongVibrationClick(View view, Runnable onClick) {
        view.setOnLongClickListener(v -> {
            AndroidUtils.playShortVibration(view.getContext());
            onClick.run();
            return true;
        });
    }

    public static ListPosition getListPosition(LinearLayoutManager layoutManager) {
        int position = layoutManager.findFirstVisibleItemPosition();
        View v = layoutManager.findViewByPosition(position);
        int offset = 0;
        if (v != null) {
            offset = v.getTop();
        }
        return new ListPosition(position, offset);
    }

    public static void scrollToPosition(LinearLayoutManager lm, ListPosition listPosition) {
        lm.scrollToPositionWithOffset(listPosition.getPosition(), listPosition.getOffset());
    }

}
