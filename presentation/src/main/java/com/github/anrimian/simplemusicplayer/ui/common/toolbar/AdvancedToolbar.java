package com.github.anrimian.simplemusicplayer.ui.common.toolbar;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;

import static android.text.TextUtils.isEmpty;

public class AdvancedToolbar extends Toolbar {

    private TextView tvTitle;
    private TextView tvSubtitle;
    private View titleContainer;
    private View actionIcon;

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
        titleContainer.setOnClickListener(listener);
    }
}
