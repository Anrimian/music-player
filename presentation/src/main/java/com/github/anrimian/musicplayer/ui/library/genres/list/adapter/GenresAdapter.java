package com.github.anrimian.musicplayer.ui.library.genres.list.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.domain.models.utils.GenreHelper;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter;

import java.util.List;

public class GenresAdapter extends DiffListAdapter<Genre, GenreViewHolder> {

    private final Callback<Genre> onClickListener;

    public GenresAdapter(RecyclerView recyclerView, Callback<Genre> onClickListener) {
        super(recyclerView, new SimpleDiffItemCallback<>(
                GenreHelper::areSourcesTheSame,
                GenreHelper::getChangePayload)
        );
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GenreViewHolder(parent, onClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        holder.update(getItem(position), payloads);
    }
}
