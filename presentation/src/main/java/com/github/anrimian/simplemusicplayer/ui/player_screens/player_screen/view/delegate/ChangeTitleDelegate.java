package com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen.view.delegate;

import android.support.constraint.ConstraintLayout;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.ui.utils.views.delegate.BottomSheetDelegate;

import static android.support.v4.view.ViewCompat.isLaidOut;

/**
 * Created on 21.01.2018.
 */

public class ChangeTitleDelegate implements BottomSheetDelegate {

    private static final int UNDEFINED = -1;

    private int expandLeftMargin = UNDEFINED;
    private int collapseRightMargin = UNDEFINED;
    private int expandTopMargin = UNDEFINED;
    private int baseRightMargin;
    private int baseTopMargin;

    private float startTextSize = 0;
    private float targetTextSize = 0;

    private TextView tvTitle;
    private ImageView btnActionsMenu;
    private ImageView ivSkipToPrevious;

    public ChangeTitleDelegate(TextView tvTitle, ImageView btnActionsMenu, ImageView ivSkipToPrevious) {
        this.tvTitle = tvTitle;
        this.btnActionsMenu = btnActionsMenu;
        this.ivSkipToPrevious = ivSkipToPrevious;
    }

    @Override
    public void onSlide(float slideOffset) {
        if (isLaidOut(tvTitle) || isLaidOut(btnActionsMenu) || isLaidOut(ivSkipToPrevious)) {
            moveView(slideOffset);
        } else {
            tvTitle.post(() -> moveView(slideOffset));
        }
    }

    private void moveView(float slideOffset) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvTitle.getLayoutParams();

        int leftMargin = params.leftMargin;
        if (expandLeftMargin == UNDEFINED) {
            expandLeftMargin = leftMargin;
        }
        params.leftMargin = expandLeftMargin * (int) (slideOffset * 100) / 100;

        if (collapseRightMargin == UNDEFINED) {
            collapseRightMargin = getCollapseRightMargin();
            baseRightMargin = params.rightMargin;
        }
        params.rightMargin = (collapseRightMargin * (int) ((1 - slideOffset) * 100) / 100) + baseRightMargin;

        if (expandTopMargin == UNDEFINED) {
            expandTopMargin = tvTitle.getContext().getResources().getDimensionPixelSize(R.dimen.content_vertical_margin);
            baseTopMargin = params.topMargin;
        }
        params.topMargin = (int) (baseTopMargin + ((expandTopMargin - baseTopMargin) * slideOffset));

        tvTitle.setLayoutParams(params);

        if (startTextSize == 0) {
            startTextSize = tvTitle.getTextSize();
            targetTextSize = tvTitle.getContext().getResources().getDimensionPixelSize(R.dimen.target_title_text_size);
        }

        float resultTextSize = (startTextSize + ((targetTextSize - startTextSize) * slideOffset));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, resultTextSize);
    }

    private int getCollapseRightMargin() {
        return (int) (btnActionsMenu.getX() - ivSkipToPrevious.getX());
    }
}
