package com.github.anrimian.simplemusicplayer.ui.library.storage.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.utils.OnItemClickListener;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 31.10.2017.
 */

class FolderViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_path)
    TextView tvPath;

    private String path;

    FolderViewHolder(View itemView, OnItemClickListener<String> onFolderClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        if (onFolderClickListener != null) {
            itemView.setOnClickListener(v -> onFolderClickListener.onItemClick(path));
        }
    }

    void bind(@Nonnull String path) {
        this.path = path;
        String displayPath = path;
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            displayPath = path.substring(++lastSlashIndex, path.length());
        }
        tvPath.setText(displayPath);
    }
}
