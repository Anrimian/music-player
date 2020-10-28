package com.github.anrimian.musicplayer.ui.common.view;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;

public class ViewUtils {

    public static ListPosition getListPosition(LinearLayoutManager layoutManager) {
        int position = layoutManager.findFirstVisibleItemPosition();
        View v = layoutManager.findViewByPosition(position);
        int offset = v.getTop();
        return new ListPosition(position, offset);
    }

    public static void scrollToPosition(LinearLayoutManager lm, ListPosition listPosition) {
        lm.scrollToPositionWithOffset(listPosition.getPosition(), listPosition.getOffset());
    }

}
