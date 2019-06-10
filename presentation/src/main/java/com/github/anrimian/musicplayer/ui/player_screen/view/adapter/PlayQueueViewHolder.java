package com.github.anrimian.musicplayer.ui.player_screen.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop.DragListener;

import javax.annotation.Nonnull;

import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
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
    private Composition composition;

    private OnItemClickListener<Composition> onDeleteCompositionClickListener;
    private OnItemClickListener<Composition> onAddToPlaylistClickListener;
    private OnItemClickListener<Composition> onShareClickListener;
    private OnItemClickListener<PlayQueueItem> onDeleteItemClickListener;

    PlayQueueViewHolder(LayoutInflater inflater,
                        ViewGroup parent,
                        OnPositionItemClickListener<PlayQueueItem> onCompositionClickListener,
                        OnItemClickListener<Composition> onDeleteCompositionClickListener,
                        OnItemClickListener<Composition> onAddToPlaylistClickListener,
                        OnItemClickListener<Composition> onShareClickListener,
                        OnItemClickListener<PlayQueueItem> onDeleteItemClickListener) {
        super(inflater.inflate(R.layout.item_play_queue, parent, false));
        ButterKnife.bind(this, itemView);
        compositionItemWrapper = new CompositionItemWrapper(itemView);

        if (onCompositionClickListener != null) {
            clickableItem.setOnClickListener(v ->
                    onCompositionClickListener.onItemClick(getAdapterPosition(), playQueueItem));
        }
        btnActionsMenu.setOnClickListener(this::onActionsMenuButtonClicked);
        this.onDeleteCompositionClickListener = onDeleteCompositionClickListener;
        this.onAddToPlaylistClickListener = onAddToPlaylistClickListener;
        this.onShareClickListener = onShareClickListener;
        this.onDeleteItemClickListener = onDeleteItemClickListener;
    }

    @Override
    public void onDragStateChanged(boolean dragging) {
        compositionItemWrapper.showAsDraggingItem(dragging);
    }

    void bind(@Nonnull PlayQueueItem item, boolean showCovers) {
        this.playQueueItem = item;
        this.composition = item.getComposition();
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

    private void onActionsMenuButtonClicked(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.play_queue_item_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_add_to_playlist: {
                    onAddToPlaylistClickListener.onItemClick(composition);
                    return true;
                }
                case R.id.menu_share: {
                    onShareClickListener.onItemClick(composition);
                    return true;
                }
                case R.id.menu_delete_from_queue: {
                    onDeleteItemClickListener.onItemClick(playQueueItem);
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

    private Context getContext() {
        return itemView.getContext();
    }


}
