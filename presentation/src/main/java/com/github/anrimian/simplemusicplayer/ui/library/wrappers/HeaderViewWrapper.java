package com.github.anrimian.simplemusicplayer.ui.library.wrappers;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

/**
 * Created on 01.11.2017.
 */

public class HeaderViewWrapper {

    @BindView(R.id.tv_parent_path)
    TextView tvParentPath;

    @BindView(R.id.clickable_item)
    View clickableItem;

    private View view;

    public HeaderViewWrapper(View view) {
        this.view = view;
        ButterKnife.bind(this, view);
    }

    public void bind(@NonNull String path) {
        String displayPath = path;
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            displayPath = path.substring(++lastSlashIndex, path.length());
        }
//        int firstSlashIndex = path.indexOf('/');
//        displayPath = path.substring(++firstSlashIndex, path.length());
        tvParentPath.setText(displayPath);
    }

    public void setOnClickListener(OnClickListener listener) {
        clickableItem.setOnClickListener(listener);
    }

    public void setVisible(boolean visible) {
        view.setVisibility(visible? VISIBLE: GONE);
    }

    public void restoreTransitionInfo() {
        ViewCompat.setTransitionName(tvParentPath, tvParentPath.getContext().getString(R.string.path_transition_element));
    }

    public void clearTransitionInfo() {
        ViewCompat.setTransitionName(tvParentPath, null);
    }
}
