package com.github.anrimian.musicplayer.ui.library.folders.wrappers;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.getLastPathSegment;

/**
 * Created on 01.11.2017.
 */

public class HeaderViewWrapper {

    @BindView(R.id.tv_parent_path)
    TextView tvParentPath;

    @BindView(R.id.header_clickable_item)
    View clickableItem;

    private View view;

    public HeaderViewWrapper(View view) {
        this.view = view;
        ButterKnife.bind(this, view);
    }

    public void bind(@NonNull String path) {
        tvParentPath.setText(getLastPathSegment(path));
    }

    public void setOnClickListener(OnClickListener listener) {
        clickableItem.setOnClickListener(listener);
    }

    public void setVisible(boolean visible) {
        view.setVisibility(visible? VISIBLE: GONE);
    }

}
