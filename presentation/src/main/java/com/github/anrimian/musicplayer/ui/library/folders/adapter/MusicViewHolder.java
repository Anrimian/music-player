package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;

import javax.annotation.Nonnull;

import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 31.10.2017.
 */

public class MusicViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.clickable_item)
    View clickableItem;

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    private CompositionItemWrapper compositionItemWrapper;

    private Composition composition;

    public MusicViewHolder(LayoutInflater inflater,
                           ViewGroup parent,
                           OnItemClickListener<Composition> onCompositionClickListener,
                           OnItemClickListener<Integer> onPositionClickListener,
                           OnViewItemClickListener<Composition> onMenuClickListener) {
        super(inflater.inflate(R.layout.item_storage_music, parent, false));
        ButterKnife.bind(this, itemView);
        compositionItemWrapper = new CompositionItemWrapper(itemView);

        if (onCompositionClickListener != null) {
            clickableItem.setOnClickListener(v ->
                    onCompositionClickListener.onItemClick(composition)
            );
        }
        if (onPositionClickListener != null) {
            clickableItem.setOnClickListener(v ->
                    onPositionClickListener.onItemClick(getAdapterPosition())
            );
        }
        btnActionsMenu.setOnClickListener(v -> onMenuClickListener.onItemClick(v, composition));
    }

    public void bind(@Nonnull Composition composition) {
        this.composition = composition;
        compositionItemWrapper.bind(composition);
    }

    private Context getContext() {
        return itemView.getContext();
    }
}
