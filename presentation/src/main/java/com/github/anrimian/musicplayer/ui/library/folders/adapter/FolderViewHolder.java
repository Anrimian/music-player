package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.domain.utils.FileUtils.getFileName;


/**
 * Created on 31.10.2017.
 */

class FolderViewHolder extends BaseViewHolder {

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

    FolderViewHolder(ViewGroup parent,
                     OnItemClickListener<String> onFolderClickListener,
                     OnViewItemClickListener<FolderFileSource> onMenuClickListener) {
        super(parent, R.layout.item_storage_folder);
        ButterKnife.bind(this, itemView);
        if (onFolderClickListener != null) {
            clickableItemView.setOnClickListener(v -> onFolderClickListener.onItemClick(path));
        }
        btnActionsMenu.setOnClickListener(v -> onMenuClickListener.onItemClick(v, folder));
    }

    void bind(@Nonnull FolderFileSource folderFileSource) {
        this.folder = folderFileSource;
        this.path = folderFileSource.getFullPath();
        String displayPath = getFileName(path);
        tvFolderName.setText(displayPath);

        int filesCount = folderFileSource.getFilesCount();
        String text = getContext().getResources().getQuantityString(R.plurals.compositions_count, filesCount, filesCount);
        tvCompositionsCount.setText(text);
    }
}
