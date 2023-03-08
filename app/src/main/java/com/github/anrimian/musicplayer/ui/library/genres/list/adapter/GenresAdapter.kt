package com.github.anrimian.musicplayer.ui.library.genres.list.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.domain.models.utils.GenreHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter

class GenresAdapter(
    recyclerView: RecyclerView,
    private val onClickListener: (Genre) -> Unit,
    private val longClickListener: (Genre) -> Unit
) : DiffListAdapter<Genre, GenreViewHolder>(recyclerView, SimpleDiffItemCallback(
    GenreHelper::areSourcesTheSame, GenreHelper::getChangePayload)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        return GenreViewHolder(parent, onClickListener, longClickListener)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: GenreViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        holder.update(getItem(position), payloads)
    }

}