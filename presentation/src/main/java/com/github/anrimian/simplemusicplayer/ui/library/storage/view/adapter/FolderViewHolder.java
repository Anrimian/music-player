package com.github.anrimian.simplemusicplayer.ui.library.storage.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 31.10.2017.
 */

class FolderViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_path)
    TextView tvPath;

    FolderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    void bind(@Nonnull String path) {
        tvPath.setText(path);
    }
}
