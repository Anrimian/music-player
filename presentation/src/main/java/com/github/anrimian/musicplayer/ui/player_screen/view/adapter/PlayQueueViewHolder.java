package com.github.anrimian.musicplayer.ui.player_screen.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop.DragListener;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 31.10.2017.
 */

class PlayQueueViewHolder extends RecyclerView.ViewHolder implements DragListener {

    @BindView(R.id.clickable_item)
    View clickableItem;

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    private CompositionItemWrapper compositionItemWrapper;

    private PlayQueueItem playQueueItem;

    PlayQueueViewHolder(LayoutInflater inflater,
                        ViewGroup parent,
                        OnPositionItemClickListener<PlayQueueItem> onCompositionClickListener,
                        OnViewItemClickListener<PlayQueueItem> menuClickListener) {
        super(inflater.inflate(R.layout.item_play_queue, parent, false));
        ButterKnife.bind(this, itemView);
        compositionItemWrapper = new CompositionItemWrapper(itemView);

        if (onCompositionClickListener != null) {
            clickableItem.setOnClickListener(v ->
                    onCompositionClickListener.onItemClick(getAdapterPosition(), playQueueItem));
        }
        btnActionsMenu.setOnClickListener(v -> menuClickListener.onItemClick(v, playQueueItem));
    }

    @Override
    public void onDragStateChanged(boolean dragging) {
        compositionItemWrapper.showAsDraggingItem(dragging);
    }

    void bind(@Nonnull PlayQueueItem item, boolean showCovers) {
        this.playQueueItem = item;
        Composition composition = item.getComposition();
        compositionItemWrapper.bind(composition, showCovers);
//        compositionItemWrapper.showNumber(getAdapterPosition());
    }

    void setCoversVisible(boolean visible) {
        compositionItemWrapper.showCompositionImage(visible);
    }

    void showAsPlayingComposition(boolean show) {
        compositionItemWrapper.showAsPlayingComposition(show);
    }

    PlayQueueItem getPlayQueueItem() {
        return playQueueItem;
    }
}
