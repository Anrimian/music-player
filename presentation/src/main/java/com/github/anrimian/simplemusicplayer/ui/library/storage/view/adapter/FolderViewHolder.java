package com.github.anrimian.simplemusicplayer.ui.library.storage.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.files.FolderFileSource;
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

    void bind(@Nonnull FolderFileSource folderFileSource) {
        this.path = folderFileSource.getPath();
        String displayPath = path;
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            displayPath = path.substring(++lastSlashIndex, path.length());
        }
        tvFolderName.setText(displayPath);

        int filesCount = folderFileSource.getFilesCount();
        String text = getContext().getResources().getQuantityString(R.plurals.compositions_count, filesCount, filesCount);
        tvCompositionsCount.setText(text);
    }

    private Context getContext() {
        return itemView.getContext();
    }
}
