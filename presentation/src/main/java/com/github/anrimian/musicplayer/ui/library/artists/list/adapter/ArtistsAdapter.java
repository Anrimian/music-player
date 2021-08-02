package com.github.anrimian.musicplayer.ui.library.artists.list.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.artist.Artist;
import com.github.anrimian.musicplayer.domain.models.utils.ArtistHelper;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

import java.util.List;

public class ArtistsAdapter extends DiffListAdapter<Artist, ArtistViewHolder> {

    private final Callback<Artist> onClickListener;
    private final OnViewItemClickListener<Artist> onItemMenuClickListener;

    public ArtistsAdapter(RecyclerView recyclerView,
                          Callback<Artist> onClickListener,
                          OnViewItemClickListener<Artist> onItemMenuClickListener) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                ArtistHelper::areSourcesTheSame,
                ArtistHelper::getChangePayload)
        );
        this.onClickListener = onClickListener;
        this.onItemMenuClickListener = onItemMenuClickListener;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ArtistViewHolder(parent, onClickListener, onItemMenuClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        holder.update(getItem(position), payloads);
    }
}
