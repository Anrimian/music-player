package com.github.anrimian.simplemusicplayer.utils.views.bottom_sheet;

import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.widget.TextView;

/**
 * Created on 21.01.2018.
 */

public class ChangeTitleDelegate implements BottomSheetDelegate {

    private static final int UNDEFINED = -1;

    private int expandLeftMargin = UNDEFINED;
    private int expandRightMargin = UNDEFINED;

    private TextView tvTitle;

    public ChangeTitleDelegate(TextView tvTitle) {
        this.tvTitle = tvTitle;
    }

    @Override
    public void onSlide(float slideOffset) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvTitle.getLayoutParams();

        int leftMargin = params.leftMargin;
        if (expandLeftMargin == UNDEFINED) {
            expandLeftMargin = leftMargin;
        }
        params.leftMargin = expandLeftMargin * (int) (slideOffset * 100) / 100;
        Log.d("KEK", "leftMargin: " + params.leftMargin);
        int rightMargin = params.rightMargin;
        if (expandRightMargin == UNDEFINED) {
            expandRightMargin = rightMargin;
        }
        params.rightMargin = expandRightMargin * (int) (slideOffset * 100) / 100;
        Log.d("KEK", "rightMargin: " + params.rightMargin);

        tvTitle.setLayoutParams(params);
    }
}
