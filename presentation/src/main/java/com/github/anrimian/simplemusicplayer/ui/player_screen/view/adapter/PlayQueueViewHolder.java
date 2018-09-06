package com.github.anrimian.simplemusicplayer.ui.player_screen.view.adapter;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.simplemusicplayer.ui.utils.OnPositionItemClickListener;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v4.graphics.ColorUtils.setAlphaComponent;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatCompositionName;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatMilliseconds;
import static com.github.anrimian.simplemusicplayer.utils.AndroidUtils.getColorFromAttr;

/**
 * Created on 31.10.2017.
 */

class PlayQueueViewHolder extends RecyclerView.ViewHolder {

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
    private OnItemClickListener<Composition> onAddToPlaylistClickListener;

    PlayQueueViewHolder(LayoutInflater inflater,
                        ViewGroup parent,
                        OnPositionItemClickListener<Composition> onCompositionClickListener,
                        OnItemClickListener<Composition> onDeleteCompositionClickListener,
                        OnItemClickListener<Composition> onAddToPlaylistClickListener) {
        super(inflater.inflate(R.layout.item_play_queue_music, parent, false));
        ButterKnife.bind(this, itemView);
        if (onCompositionClickListener != null) {
            clickableItem.setOnClickListener(v ->
                    onCompositionClickListener.onItemClick(getAdapterPosition(), composition));
        }
        btnActionsMenu.setOnClickListener(this::onActionsMenuButtonClicked);
        this.onDeleteCompositionClickListener = onDeleteCompositionClickListener;
        this.onAddToPlaylistClickListener = onAddToPlaylistClickListener;
    }

    void bind(@Nonnull Composition composition) {
        this.composition = composition;
        tvMusicName.setText(formatCompositionName(composition));
        showAdditionalInfo();

        int textColorAttr = composition.isCorrupted()? android.R.attr.textColorSecondary:
                android.R.attr.textColorPrimary;

        tvMusicName.setTextColor(getColorFromAttr(getContext(), textColorAttr));
    }

    void showAsPlayingComposition(boolean show) {
        int alpha = show? 20: 0;
        int color = setAlphaComponent(getColorFromAttr(getContext(), R.attr.colorPrimary), alpha);
        clickableItem.setBackgroundColor(color);
        clickableItem.setClickable(!show);
    }

    private void showAdditionalInfo() {
        StringBuilder sb = formatCompositionAuthor(composition, getContext());
        sb.append(" ● ");//TODO split problem
        sb.append(formatMilliseconds(composition.getDuration()));
        tvAdditionalInfo.setText(sb.toString());
    }

    private void onActionsMenuButtonClicked(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.composition_in_folder_actions_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_add_to_playlist: {
                    onAddToPlaylistClickListener.onItemClick(composition);
                    return true;
                }
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

    private String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }

    private Context getContext() {
        return itemView.getContext();
    }


}
