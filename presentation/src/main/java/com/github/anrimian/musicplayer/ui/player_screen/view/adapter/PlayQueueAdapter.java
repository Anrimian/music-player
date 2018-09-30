package com.github.anrimian.musicplayer.ui.player_screen.view.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.utils.PlayQueueItemHelper;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffCallback;

import java.util.List;

import javax.annotation.Nullable;

import static android.support.v7.util.DiffUtil.calculateDiff;

/**
 * Created on 31.10.2017.
 */

public class PlayQueueAdapter extends RecyclerView.Adapter<PlayQueueViewHolder> {

    private static final String CURRENT_COMPOSITION_CHANGED = "current_composition_changed";

    private final List<PlayQueueItem> musicList;
    private OnPositionItemClickListener<PlayQueueItem> onCompositionClickListener;
    private OnItemClickListener<Composition> onDeleteCompositionClickListener;
    private OnItemClickListener<Composition> onAddToPlaylistClickListener;

    @Nullable
    private PlayQueueItem currentItem;

    public PlayQueueAdapter(List<PlayQueueItem> musicList) {
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public PlayQueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayQueueViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onCompositionClickListener,
                onDeleteCompositionClickListener,
                onAddToPlaylistClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayQueueViewHolder holder, int position) {
        PlayQueueItem composition = musicList.get(position);
        holder.bind(composition);
        holder.showAsPlayingComposition(composition.equals(currentItem));
    }

    @Override
    public void onBindViewHolder(@NonNull PlayQueueViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        }
        for (Object payload: payloads) {
            if (payload == CURRENT_COMPOSITION_CHANGED) {
                PlayQueueItem item = musicList.get(position);
                holder.showAsPlayingComposition(item.equals(currentItem));
            }
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    //TODO optimize
    public void onCurrentItemChanged(PlayQueueItem item) {
        int oldPosition = musicList.indexOf(currentItem);
        currentItem = item;
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition, CURRENT_COMPOSITION_CHANGED);
        }
        notifyItemChanged(musicList.indexOf(item), CURRENT_COMPOSITION_CHANGED);

    }

    public void updatePlayList(List<PlayQueueItem> oldPlayList, List<PlayQueueItem> newPlayList) {
        calculateDiff(new SimpleDiffCallback<>(
                oldPlayList, newPlayList, PlayQueueItemHelper::areSourcesTheSame), false)
                .dispatchUpdatesTo(this);
    }

    public void setOnCompositionClickListener(OnPositionItemClickListener<PlayQueueItem> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnDeleteCompositionClickListener(OnItemClickListener<Composition> onDeleteCompositionClickListener) {
        this.onDeleteCompositionClickListener = onDeleteCompositionClickListener;
    }

    public void setOnAddToPlaylistClickListener(OnItemClickListener<Composition> onAddToPlaylistClickListener) {
        this.onAddToPlaylistClickListener = onAddToPlaylistClickListener;
    }
}
