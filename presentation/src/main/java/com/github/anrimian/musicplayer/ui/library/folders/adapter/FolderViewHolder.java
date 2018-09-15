package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.OnTransitionItemClickListener;

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

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    private String path;

    private OnItemClickListener<String> onDeleteCompositionClickListener;
    private OnItemClickListener<String> onAddToPlaylistClickListener;

    FolderViewHolder(LayoutInflater inflater,
                     ViewGroup parent,
                     OnItemClickListener<String> onFolderClickListener,
                     OnItemClickListener<String> onDeleteCompositionClickListener,
                     OnItemClickListener<String> onAddToPlaylistClickListener) {
        super(inflater.inflate(R.layout.item_storage_folder, parent, false));
        ButterKnife.bind(this, itemView);
        if (onFolderClickListener != null) {
            clickableItemView.setOnClickListener(v -> onFolderClickListener.onItemClick(path));
        }
        btnActionsMenu.setOnClickListener(this::onActionsMenuButtonClicked);
        this.onDeleteCompositionClickListener = onDeleteCompositionClickListener;
        this.onAddToPlaylistClickListener = onAddToPlaylistClickListener;
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

    private void onActionsMenuButtonClicked(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.folder_item_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_add_to_playlist: {
                    onAddToPlaylistClickListener.onItemClick(path);
                    return true;
                }
                case R.id.menu_share: {
//                    presenter.onShareCompositionButtonClicked();
                    return true;
                }
                case R.id.menu_delete: {
                    onDeleteCompositionClickListener.onItemClick(path);
                    return true;
                }
            }
            return false;
        });
        popup.show();
    }

    private Context getContext() {
        return itemView.getContext();
    }
}
