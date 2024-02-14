package com.github.anrimian.musicplayer.ui.editor.composition.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.models.utils.ShortGenreHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.adapter.DiffListAdapter

class GenreChipsAdapter(
    recyclerView: RecyclerView,
    private val onClickListener: (String) -> Unit,
    private val onRemoveClickListener: (String) -> Unit,
) : DiffListAdapter<String, GenreChipViewHolder>(
    recyclerView,
    SimpleDiffItemCallback(ShortGenreHelper::areSourcesTheSame, ShortGenreHelper::getChangePayload)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreChipViewHolder {
        return GenreChipViewHolder(parent, onClickListener, onRemoveClickListener)
    }

    override fun onBindViewHolder(holder: GenreChipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: GenreChipViewHolder,
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