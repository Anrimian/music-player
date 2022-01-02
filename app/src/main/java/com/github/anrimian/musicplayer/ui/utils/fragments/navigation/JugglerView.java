package com.github.anrimian.musicplayer.ui.utils.fragments.navigation;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;


public class JugglerView extends FrameLayout {

    private static final String FIRST_VIEW_ID = "jv_first_view_id";
    private static final String SECOND_VIEW_ID = "jv_second_view_id";
    private static final String TOP_VIEW_ID = "jv_top_view_id";
    private static final String BOTTOM_VIEW_ID = "jv_bottom_view_id";

    private int firstViewId;
    private int secondViewId;

    private int topViewId ;
    private int bottomViewId;

    private FrameLayout firstView;
    private FrameLayout secondView;

    public JugglerView(@NonNull Context context) {
        this(context, null);
    }

    public JugglerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JugglerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(@Nullable Bundle savedState) {
        if (savedState == null) {
            firstViewId = ViewCompat.generateViewId();
            secondViewId = ViewCompat.generateViewId();

            topViewId = secondViewId;
            bottomViewId = firstViewId;
        } else {
            firstViewId = savedState.getInt(FIRST_VIEW_ID);
            secondViewId = savedState.getInt(SECOND_VIEW_ID);
            topViewId = savedState.getInt(TOP_VIEW_ID);
            bottomViewId = savedState.getInt(BOTTOM_VIEW_ID);
        }
        initChildViews();

    }

    public void saveInstanceState(Bundle state) {
        state.putInt(FIRST_VIEW_ID, firstViewId);
        state.putInt(SECOND_VIEW_ID, secondViewId);
        state.putInt(TOP_VIEW_ID, topViewId);
        state.putInt(BOTTOM_VIEW_ID, bottomViewId);
    }

    int prepareTopView() {
        View viewToTop = topViewId == firstViewId? secondView: firstView;

        ((FrameLayout) viewToTop).removeAllViews();
        removeView(viewToTop);
        addView(viewToTop);
        int topViewId = viewToTop.getId();
        onTopViewIdSelected(topViewId);
        return topViewId;
    }

    int prepareBottomView() {
        View viewToBottom;
        int topViewId;
        if (this.topViewId == firstViewId) {
            viewToBottom = firstView;
            topViewId = secondViewId;
        } else {
            viewToBottom = secondView;
            topViewId = firstViewId;
        }

        ((FrameLayout) viewToBottom).removeAllViews();
        removeView(viewToBottom);
        addView(viewToBottom, 0);

        onTopViewIdSelected(topViewId);

        return viewToBottom.getId();
    }

    int getTopViewId() {
        return topViewId;
    }

    int getBottomViewId() {
        return bottomViewId;
    }

    private void initChildViews() {
        firstView = createFrameLayout();
        firstView.setId(firstViewId);
        secondView = createFrameLayout();
        secondView.setId(secondViewId);

        if (topViewId == secondViewId) {
            addView(firstView);
            addView(secondView);
        } else {
            addView(secondView);
            addView(firstView);
        }
    }

    private FrameLayout createFrameLayout() {
        FrameLayout frameLayout = new FrameLayout(getContext());
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        frameLayout.setLayoutParams(params);
        return frameLayout;
    }

    private void onTopViewIdSelected(int topViewId) {
        if (this.topViewId != topViewId) {
            bottomViewId = this.topViewId;
            this.topViewId = topViewId;
        }
    }




}
