package com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewPositionItemClickListener;

import javax.annotation.Nonnull;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 31.10.2017.
 */

public class PlayListItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.clickable_item)
    View clickableItem;

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    private CompositionItemWrapper compositionItemWrapper;

    private PlayListItem item;

    PlayListItemViewHolder(LayoutInflater inflater,
                           ViewGroup parent,
                           OnItemClickListener<Integer> onCompositionClickListener,
                           OnViewPositionItemClickListener<PlayListItem> onMenuClickListener) {
        super(inflater.inflate(R.layout.item_storage_music, parent, false));
        ButterKnife.bind(this, itemView);
        compositionItemWrapper = new CompositionItemWrapper(itemView);

        if (onCompositionClickListener != null) {
            clickableItem.setOnClickListener(v ->
                    onCompositionClickListener.onItemClick(getAdapterPosition())
            );
        }
        btnActionsMenu.setOnClickListener(v ->
                onMenuClickListener.onItemClick(v, item, getAdapterPosition())
        );
    }

    public void bind(@Nonnull PlayListItem item, boolean coversEnabled) {
        this.item = item;
        Composition composition = item.getComposition();
        compositionItemWrapper.bind(composition, coversEnabled);
    }

    private String getString(@StringRes int resId) {
        return getContext().getString(resId);
    }

    private Context getContext() {
        return itemView.getContext();
    }
}
