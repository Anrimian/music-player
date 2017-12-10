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

    @BindView(R.id.clickable_item)
    View clickableItemView;

    @BindView(R.id.tv_folder_name)
    TextView tvFolderName;

    @BindView(R.id.tv_compositions_count)
    TextView tvCompositionsCount;

    private String path;

    FolderViewHolder(View itemView, OnItemClickListener<String> onFolderClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        if (onFolderClickListener != null) {
            clickableItemView.setOnClickListener(v -> onFolderClickListener.onItemClick(path));
        }
    }

    void bind(@Nonnull String path) {
        this.path = path;
        String displayPath = path;
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            displayPath = path.substring(++lastSlashIndex, path.length());
        }
        tvFolderName.setText(displayPath);

        tvCompositionsCount.setText("unknown");
    }
}
