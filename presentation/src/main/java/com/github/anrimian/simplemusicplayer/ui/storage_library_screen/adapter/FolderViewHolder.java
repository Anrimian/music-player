package com.github.anrimian.simplemusicplayer.ui.storage_library_screen.adapter;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.ui.utils.views.recycler_view.OnTransitionItemClickListener;

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

    FolderViewHolder(View itemView, OnTransitionItemClickListener<String> onFolderClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        if (onFolderClickListener != null) {
            clickableItemView.setOnClickListener(v -> {
                ViewCompat.setTransitionName(tvFolderName, getContext().getString(R.string.path_transition_element));
                onFolderClickListener.onItemClick(path, tvFolderName);
            });
        }
    }

    void bind(@Nonnull FolderFileSource folderFileSource) {
        this.path = folderFileSource.getFullPath();
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
