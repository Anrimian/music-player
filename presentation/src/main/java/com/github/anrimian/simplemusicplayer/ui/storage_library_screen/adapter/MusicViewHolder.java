package com.github.anrimian.simplemusicplayer.ui.storage_library_screen.adapter;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.utils.OnItemClickListener;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatCompositionName;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.simplemusicplayer.utils.AndroidUtils.getColorFromAttr;

/**
 * Created on 31.10.2017.
 */

class MusicViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_composition_name)
    TextView tvMusicName;

    @BindView(R.id.tv_additional_info)
    TextView tvAdditionalInfo;

    @BindView(R.id.clickable_item)
    View clickableItem;

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    private Composition composition;

    private OnItemClickListener<Composition> onDeleteCompositionClickListener;

    MusicViewHolder(View itemView,
                    OnItemClickListener<Composition> onCompositionClickListener,
                    OnItemClickListener<Composition> onDeleteCompositionClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        if (onCompositionClickListener != null) {
            clickableItem.setOnClickListener(v -> onCompositionClickListener.onItemClick(composition));
        }
        btnActionsMenu.setOnClickListener(this::onActionsMenuButtonClicked);
        this.onDeleteCompositionClickListener = onDeleteCompositionClickListener;
    }

    private void onActionsMenuButtonClicked(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.composition_in_folder_actions_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_share: {
//                    presenter.onShareCompositionButtonClicked();
                    return true;
                }
                case R.id.menu_delete: {
                    onDeleteCompositionClickListener.onItemClick(composition);
                    return true;
                }
            }
            return false;
        });
        popup.show();
    }

    void bind(@Nonnull Composition composition) {
        this.composition = composition;
        tvMusicName.setText(formatCompositionName(composition));
        showAdditionalInfo();

        int textColorAttr = composition.isCorrupted()? android.R.attr.textColorSecondary:
                android.R.attr.textColorPrimary;

        tvMusicName.setTextColor(getColorFromAttr(getContext(), textColorAttr));
    }

    private void showAdditionalInfo() {
        StringBuilder sb = formatCompositionAuthor(composition, getContext());
        sb.append(" ● ");//TODO split problem
        sb.append(formatMilliseconds(composition.getDuration()));
        tvAdditionalInfo.setText(sb.toString());
    }

    private String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }

    private Context getContext() {
        return itemView.getContext();
    }
}
