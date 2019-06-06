package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;

import javax.annotation.Nonnull;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.domain.models.composition.CompositionModelHelper.getLastPathPart;

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

    private FolderFileSource folder;
    private String path;

    FolderViewHolder(LayoutInflater inflater,
                     ViewGroup parent,
                     OnItemClickListener<String> onFolderClickListener,
                     OnViewItemClickListener<FolderFileSource> onMenuClickListener) {
        super(inflater.inflate(R.layout.item_storage_folder, parent, false));
        ButterKnife.bind(this, itemView);
        if (onFolderClickListener != null) {
            clickableItemView.setOnClickListener(v -> onFolderClickListener.onItemClick(path));
        }
        btnActionsMenu.setOnClickListener(v -> onMenuClickListener.onItemClick(v, folder));
    }

    void bind(@Nonnull FolderFileSource folderFileSource) {
        this.folder = folderFileSource;
        this.path = folderFileSource.getFullPath();
        String displayPath = getLastPathPart(path);
        tvFolderName.setText(displayPath);

        int filesCount = folderFileSource.getFilesCount();
        String text = getContext().getResources().getQuantityString(R.plurals.compositions_count, filesCount, filesCount);
        tvCompositionsCount.setText(text);
    }

    private Context getContext() {
        return itemView.getContext();
    }
}
