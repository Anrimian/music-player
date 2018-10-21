package com.github.anrimian.musicplayer.ui.utils.fragments.navigation;

import android.support.v4.view.ViewCompat;

class JugglerViewPresenter {

    private int firstViewId = ViewCompat.generateViewId();
    private int secondViewId = ViewCompat.generateViewId();

    private int topViewId = secondViewId;
    private int bottomViewId = firstViewId;

    void initializeView(JugglerView view) {
        view.init(firstViewId, secondViewId, topViewId);
    }

    int getFirstViewId() {
        return firstViewId;
    }

    int getSecondViewId() {
        return secondViewId;
    }

    int getTopViewId() {
        return topViewId;
    }

    int getBottomViewId() {
        return bottomViewId;
    }

    void onTopViewIdSelected(int topViewId) {
        if (this.topViewId != topViewId) {
            bottomViewId = this.topViewId;
            this.topViewId = topViewId;
        }
    }
}
