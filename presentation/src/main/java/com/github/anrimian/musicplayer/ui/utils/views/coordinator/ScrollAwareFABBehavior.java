package com.github.anrimian.musicplayer.ui.utils.views.coordinator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created on 25.02.2018.
 */

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {

    private static final int TRANSLATE_HIDE_DURATION_MILLIS = 200;
    private static final int TRANSLATE_SHOW_DURATION_MILLIS = 170;

    private Animator hideAnimator;
    private Animator showAnimator;

    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull FloatingActionButton child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target,
                                       int axes,
                                       int type) {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent,
                                   FloatingActionButton child,
                                   View dependency) {
        return dependency instanceof RecyclerView;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                               @NonNull FloatingActionButton fab,
                               @NonNull View target,
                               int dxConsumed,
                               int dyConsumed,
                               int dxUnconsumed,
                               int dyUnconsumed,
                               int type) {
        super.onNestedScroll(coordinatorLayout, fab, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed, type);
        if (type == ViewCompat.TYPE_TOUCH) {
            if (dyConsumed > 0 && hideAnimator == null) {
                if (showAnimator != null ) {
                    showAnimator.cancel();
                }

                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                float translation = fab.getHeight() +  params.bottomMargin;
                hideAnimator = ObjectAnimator.ofFloat(fab, "translationY", fab.getTranslationY(), translation);
                hideAnimator.setDuration(TRANSLATE_HIDE_DURATION_MILLIS);
                hideAnimator.setInterpolator(new AccelerateInterpolator());
                hideAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        hideAnimator = null;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        hideAnimator = null;
                    }
                });
                hideAnimator.start();
            } else if (dyConsumed < 0 && showAnimator == null) {
                if (hideAnimator != null) {
                    hideAnimator.cancel();
                }

                showAnimator = ObjectAnimator.ofFloat(fab, "translationY", fab.getTranslationY(), 0);
                showAnimator.setDuration(TRANSLATE_SHOW_DURATION_MILLIS);
                showAnimator.setInterpolator(new DecelerateInterpolator());
                showAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        super.onAnimationCancel(animation);
                        showAnimator = null;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        showAnimator = null;
                    }
                });
                showAnimator.start();
            }
        }
    }
}
