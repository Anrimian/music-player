package com.github.anrimian.musicplayer.ui.settings.folders.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.folders.IgnoredFolder;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

class ExcludedFolderViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_folder_name)
    TextView tvFolderName;

    @BindView(R.id.btn_remove)
    View btnRemove;

    private IgnoredFolder folder;

    ExcludedFolderViewHolder(@NonNull ViewGroup parent,
                             Callback<IgnoredFolder> removeClickListener) {
        super(parent, R.layout.item_excluded_folder);
        ButterKnife.bind(this, itemView);
        btnRemove.setOnClickListener(v -> removeClickListener.call(folder));
    }

    void bind(IgnoredFolder folder) {
        this.folder = folder;
        tvFolderName.setText(folder.getRelativePath());
    }
}
