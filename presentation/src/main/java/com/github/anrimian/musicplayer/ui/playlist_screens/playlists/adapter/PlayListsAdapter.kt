package com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.PlayListHelper;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

public class PlayListsAdapter extends DiffListAdapter<PlayList, PlayListViewHolder> {

    private final OnItemClickListener<PlayList> onItemClickListener;
    private final OnItemClickListener<PlayList> onItemLongClickListener;

    public PlayListsAdapter(RecyclerView recyclerView,
                            OnItemClickListener<PlayList> onItemClickListener,
                            OnItemClickListener<PlayList> onItemLongClickListener) {
        super(recyclerView, new SimpleDiffItemCallback<>(PlayListHelper::areSourcesTheSame));
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlayListViewHolder(LayoutInflater.from(parent.getContext()),
                parent,
                onItemClickListener,
                onItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {
        PlayList playList = getItem(position);
        holder.bind(playList);
    }
}


