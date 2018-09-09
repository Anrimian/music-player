package com.github.anrimian.musicplayer.ui.library.compositions.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.library.folders.adapter.MusicViewHolder;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffCallback;

import java.util.List;

import static android.support.v7.util.DiffUtil.calculateDiff;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.hasChanges;

/**
 * Created on 31.10.2017.
 */

public class CompositionsAdapter extends RecyclerView.Adapter<MusicViewHolder> {

    private final List<Composition> musicList;
    private OnItemClickListener<Composition> onCompositionClickListener;
    private OnItemClickListener<Composition> onDeleteCompositionClickListener;
    private OnItemClickListener<Composition> onAddToPlaylistClickListener;

    public CompositionsAdapter(List<Composition> musicList) {
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onCompositionClickListener,
                onDeleteCompositionClickListener,
                onAddToPlaylistClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Composition composition = musicList.get(position);
        holder.bind(composition);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public void updateList(List<Composition> oldList, List<Composition> sourceList) {
        calculateDiff(new SimpleDiffCallback<>(oldList, sourceList, this::areSourcedTheSame))
                .dispatchUpdatesTo(this);
    }

    public void setOnCompositionClickListener(OnItemClickListener<Composition> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnDeleteCompositionClickListener(OnItemClickListener<Composition> onDeleteCompositionClickListener) {
        this.onDeleteCompositionClickListener = onDeleteCompositionClickListener;
    }

    public void setOnAddToPlaylistClickListener(OnItemClickListener<Composition> onAddToPlaylistClickListener) {
        this.onAddToPlaylistClickListener = onAddToPlaylistClickListener;
    }

    private boolean areSourcedTheSame(Composition oldSource, Composition newSource) {
        return !hasChanges(oldSource, newSource);
    }
}
