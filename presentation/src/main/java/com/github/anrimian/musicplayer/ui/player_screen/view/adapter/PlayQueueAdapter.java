package com.github.anrimian.musicplayer.ui.player_screen.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Created on 31.10.2017.
 */

public class PlayQueueAdapter extends RecyclerView.Adapter<PlayQueueViewHolder> {

    private final Set<PlayQueueViewHolder> viewHolders = new HashSet<>();

    private List<PlayQueueItem> musicList;
    private OnPositionItemClickListener<PlayQueueItem> onCompositionClickListener;
    private OnViewItemClickListener<PlayQueueItem> menuClickListener;

    @Nullable
    private PlayQueueItem currentItem;

    private boolean isCoversEnabled;

    public PlayQueueAdapter(List<PlayQueueItem> musicList) {
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public PlayQueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayQueueViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onCompositionClickListener,
                menuClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayQueueViewHolder holder, int position) {
        viewHolders.add(holder);

        PlayQueueItem composition = musicList.get(position);
        holder.bind(composition, isCoversEnabled);
        holder.showAsPlayingComposition(composition.equals(currentItem));
    }

    @Override
    public void onBindViewHolder(@NonNull PlayQueueViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    @Override
    public void onViewRecycled(@NonNull PlayQueueViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    public void onCurrentItemChanged(PlayQueueItem currentItem) {
        this.currentItem = currentItem;
        for (PlayQueueViewHolder holder: viewHolders) {
            holder.showAsPlayingComposition(holder.getPlayQueueItem().equals(currentItem));
        }
    }

    public void setCoversEnabled(boolean isCoversEnabled) {
        this.isCoversEnabled = isCoversEnabled;
        for (PlayQueueViewHolder holder: viewHolders) {
            holder.setCoversVisible(isCoversEnabled);
        }
    }

    public void setItems(List<PlayQueueItem> list) {
        musicList = list;
    }

    public void setOnCompositionClickListener(OnPositionItemClickListener<PlayQueueItem> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setMenuClickListener(OnViewItemClickListener<PlayQueueItem> menuClickListener) {
        this.menuClickListener = menuClickListener;
    }
}
