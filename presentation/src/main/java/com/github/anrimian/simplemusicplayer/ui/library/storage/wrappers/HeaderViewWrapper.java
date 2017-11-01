package com.github.anrimian.simplemusicplayer.ui.library.storage.wrappers;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.*;

/**
 * Created on 01.11.2017.
 */

public class HeaderViewWrapper {

    @BindView(R.id.tv_parent_path)
    TextView tvParentPath;

    private View view;

    public HeaderViewWrapper(View view) {
        this.view = view;
        ButterKnife.bind(this, view);
    }

    public void bind(@NonNull String path) {
        String targetPath = path;
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            targetPath = path.substring(lastSlashIndex, path.length());
        }
        tvParentPath.setText(targetPath);
    }

    public void setOnClickListener(OnClickListener listener) {
        view.setOnClickListener(listener);
    }

    public void setVisible(boolean visible) {
        view.setVisibility(visible? VISIBLE: GONE);
    }
}
