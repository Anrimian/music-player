package com.github.anrimian.simplemusicplayer.ui.common.toolbar;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;

import static android.animation.ObjectAnimator.ofFloat;
import static android.text.TextUtils.isEmpty;
import static com.github.anrimian.simplemusicplayer.Constants.Animation.TOOLBAR_ARROW_ANIMATION_TIME;

public class AdvancedToolbar extends Toolbar {

    private TextView tvTitle;
    private TextView tvSubtitle;
    private View titleContainer;
    private View actionIcon;

    private FragmentManager fragmentManager;
    private DrawerArrowDrawable drawerArrowDrawable;
    private int fragmentContainerId;

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
        actionIcon.setVisibility(GONE);
        setTitle("");//setSupportActionBar() set app title to null title in action bar
    }

    public void setupWithFragmentManager(FragmentManager fragmentManager,
                                         DrawerArrowDrawable drawerArrowDrawable,
                                         int fragmentContainerId) {
        this.fragmentManager = fragmentManager;
        this.drawerArrowDrawable = drawerArrowDrawable;
        this.fragmentContainerId = fragmentContainerId;
        onFragmentStackChanged();
        fragmentManager.addOnBackStackChangedListener(this::onFragmentStackChanged);
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

    public void setUpMenu(@MenuRes int menuId, OnMenuItemClickListener listener) {
        getMenu().clear();
        inflateMenu(menuId);
        setOnMenuItemClickListener(listener);
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

    private void onFragmentStackChanged() {
        float start = drawerArrowDrawable.getProgress();
        float end = fragmentManager.getBackStackEntryCount() == 0? 0f: 1f;
        ObjectAnimator objectAnimator = ofFloat(drawerArrowDrawable, "progress", start, end);
        objectAnimator.setDuration(TOOLBAR_ARROW_ANIMATION_TIME);
        objectAnimator.start();
    }
}
