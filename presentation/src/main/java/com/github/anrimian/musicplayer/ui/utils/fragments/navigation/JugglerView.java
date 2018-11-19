package com.github.anrimian.musicplayer.ui.utils.fragments.navigation;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;


public class JugglerView extends FrameLayout {

    private FrameLayout firstView;
    private FrameLayout secondView;

    private JugglerViewPresenter presenter;

    public JugglerView(@NonNull Context context) {
        this(context, null);
    }

    public JugglerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JugglerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPresenter(JugglerViewPresenter presenter) {
        this.presenter = presenter;
    }

    void init(int firstViewId, int secondViewId, int topViewId) {
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

    int prepareTopView() {
        View viewToTop = presenter.getTopViewId() == presenter.getFirstViewId()? secondView: firstView;

        ((FrameLayout) viewToTop).removeAllViews();
        removeView(viewToTop);
        addView(viewToTop);
        int topViewId = viewToTop.getId();
        presenter.onTopViewIdSelected(topViewId);
        return topViewId;
    }

    int prepareBottomView() {
        View viewToBottom;
        int topViewId;
        if (presenter.getTopViewId() == presenter.getFirstViewId()) {
            viewToBottom = firstView;
            topViewId = presenter.getSecondViewId();
        } else {
            viewToBottom = secondView;
            topViewId = presenter.getFirstViewId();
        }

        ((FrameLayout) viewToBottom).removeAllViews();
        removeView(viewToBottom);
        addView(viewToBottom, 0);

        presenter.onTopViewIdSelected(topViewId);

        return viewToBottom.getId();
    }

    private FrameLayout createFrameLayout() {
        FrameLayout frameLayout = new FrameLayout(getContext());
        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        frameLayout.setLayoutParams(params);
        return frameLayout;
    }
}
