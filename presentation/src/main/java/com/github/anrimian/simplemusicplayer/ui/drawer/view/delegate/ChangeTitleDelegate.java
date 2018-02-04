package com.github.anrimian.simplemusicplayer.ui.drawer.view.delegate;

import android.support.constraint.ConstraintLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.utils.views.bottom_sheet.delegate.BottomSheetDelegate;

import static android.support.v4.view.ViewCompat.isLaidOut;

/**
 * Created on 21.01.2018.
 */

public class ChangeTitleDelegate implements BottomSheetDelegate {

    private static final int UNDEFINED = -1;

    private int expandLeftMargin = UNDEFINED;
    private int collapseRightMargin = UNDEFINED;
    private int baseRightMargin;

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

        tvTitle.setLayoutParams(params);
    }

    private int getCollapseRightMargin() {
        return (int) (btnActionsMenu.getX() - ivSkipToPrevious.getX());
    }
}
