package com.github.anrimian.musicplayer.ui.common.toolbar;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;

import static android.animation.ObjectAnimator.ofFloat;
import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.musicplayer.Constants.Animation.TOOLBAR_ARROW_ANIMATION_TIME;

public class AdvancedToolbar extends Toolbar {

    private TextView tvTitle;
    private TextView tvSubtitle;
    private View titleContainer;
    private View actionIcon;
    private EditText etSearch;
    private ActionMenuView actionMenuView;

    private FragmentManager fragmentManager;
    private DrawerArrowDrawable drawerArrowDrawable;
    private LockArrowInBackStateFunction lockArrowFunction;

    private boolean isInSearchMode;

    public AdvancedToolbar(Context context) {
        super(context);
    }

    public AdvancedToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvancedToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init() {
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        titleContainer = findViewById(R.id.title_container);
        actionIcon = findViewById(R.id.action_icon);
        etSearch = findViewById(R.id.et_search);
        etSearch.setVisibility(INVISIBLE);
        actionIcon.setVisibility(GONE);
        setTitle("");//setSupportActionBar() set app title to null title in action bar
    }

    public void setupWithFragmentManager(FragmentManager fragmentManager,
                                         DrawerArrowDrawable drawerArrowDrawable,
                                         LockArrowInBackStateFunction lockArrowFunction) {
        this.fragmentManager = fragmentManager;
        this.drawerArrowDrawable = drawerArrowDrawable;
        this.lockArrowFunction = lockArrowFunction;
        onFragmentStackChanged();
        fragmentManager.addOnBackStackChangedListener(this::onFragmentStackChanged);
    }

    public void setSearchModeEnabled(boolean enabled) {
        isInSearchMode = enabled;
        etSearch.setVisibility(enabled? VISIBLE: GONE);
        tvTitle.setVisibility(enabled? GONE: VISIBLE);
        tvSubtitle.setVisibility(enabled? GONE: VISIBLE);
        actionIcon.setVisibility(enabled? GONE: VISIBLE);
        getActionMenuView().setVisibility(enabled? GONE: VISIBLE);
        setCommandButtonMode(!enabled);
    }

    @Override
    public CharSequence getTitle() {
        return tvTitle.getText();
    }

    @Override
    public void setTitle(CharSequence title) {
        tvTitle.setVisibility(isEmpty(title) ? GONE : VISIBLE);
        tvTitle.setText(title);
    }

    @Override
    public CharSequence getSubtitle() {
        return tvSubtitle.getText();
    }

    @Override
    public void setSubtitle(CharSequence subtitle) {
        tvSubtitle.setVisibility(isEmpty(subtitle) ? GONE : VISIBLE);
        tvSubtitle.setText(subtitle);
    }

    public void setTitleClickListener(View.OnClickListener listener) {
        actionIcon.setVisibility(listener == null? GONE : VISIBLE);
        titleContainer.setEnabled(listener != null);
        titleContainer.setOnClickListener(listener);
    }

    public void onStackFragmentSlided(float offset) {
        if (fragmentManager.getBackStackEntryCount() == 1) {
            drawerArrowDrawable.setProgress(offset);
        }
    }

    public boolean isInSearchMode() {
        return isInSearchMode;
    }

    public ActionMenuView getActionMenuView() {
        if (actionMenuView == null) {
            actionMenuView = findActionMenuView();
            if (actionMenuView == null) {
                inflateMenu(R.menu.empty_stub_menu);
            }
            actionMenuView = findActionMenuView();
        }
        return actionMenuView;
    }

    private void onFragmentStackChanged() {
        boolean isRoot = fragmentManager.getBackStackEntryCount() == 0;
        if (isRoot && lockArrowFunction.isLocked()) {
            return;
        }

        setCommandButtonMode(isRoot);
    }

    private void setCommandButtonMode(boolean isNormal) {
        float end = isNormal? 0f : 1f;
        float start = drawerArrowDrawable.getProgress();
        ObjectAnimator objectAnimator = ofFloat(drawerArrowDrawable, "progress", start, end);
        objectAnimator.setDuration(TOOLBAR_ARROW_ANIMATION_TIME);
        objectAnimator.start();
    }

    @Nullable
    private ActionMenuView findActionMenuView() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ActionMenuView) {
                return (ActionMenuView) child;
            }
        }
        return null;
    }

    public interface LockArrowInBackStateFunction {
        boolean isLocked();
    }
}
