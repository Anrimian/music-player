package com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.ui.common.format.ImageFormatUtils;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.musicplayer.utils.AndroidUtils.getColorFromAttr;

/**
 * Created on 31.10.2017.
 */

public class PlayListItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_composition_name)
    TextView tvMusicName;

    @BindView(R.id.tv_additional_info)
    TextView tvAdditionalInfo;

    @BindView(R.id.clickable_item)
    View clickableItem;

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    @BindView(R.id.iv_music_icon)
    ImageView ivMusicIcon;

    private PlayListItem item;
    private Composition composition;

    PlayListItemViewHolder(LayoutInflater inflater,
                           ViewGroup parent,
                           OnItemClickListener<Integer> onCompositionClickListener,
                           OnViewItemClickListener<PlayListItem> onMenuClickListener) {
        super(inflater.inflate(R.layout.item_storage_music, parent, false));
        ButterKnife.bind(this, itemView);
        if (onCompositionClickListener != null) {
            clickableItem.setOnClickListener(v ->
                    onCompositionClickListener.onItemClick(getAdapterPosition())
            );
        }
        btnActionsMenu.setOnClickListener(v -> onMenuClickListener.onItemClick(v, item));
    }

    public void bind(@Nonnull PlayListItem item) {
        this.item = item;
        this.composition = item.getComposition();
        tvMusicName.setText(formatCompositionName(composition));
        showAdditionalInfo();

        int textColorAttr = composition.isCorrupted()? android.R.attr.textColorSecondary:
                android.R.attr.textColorPrimary;

        tvMusicName.setTextColor(getColorFromAttr(getContext(), textColorAttr));

        ImageFormatUtils.displayImage(ivMusicIcon, composition);
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
