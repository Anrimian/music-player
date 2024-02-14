package com.github.anrimian.musicplayer.ui.library.genres.list.adapter

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.genres.Genre
import com.github.anrimian.musicplayer.domain.models.utils.GenreHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

class GenresAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val selectedGenres: Set<Genre>,
    private val itemClickListener: (Int, Genre) -> Unit,
    private val itemLongClickListener: (Int, Genre) -> Unit,
    private val onItemMenuClickListener: (View, Genre) -> Unit
) : MvpDiffAdapter<Genre, GenreViewHolder>(
    lifecycleOwner,
    recyclerView,
    SimpleDiffItemCallback(GenreHelper::areSourcesTheSame, GenreHelper::getChangePayload)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        return GenreViewHolder(
            parent,
            itemClickListener,
            itemLongClickListener,
            onItemMenuClickListener
        )
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val genre = getItem(position)
        holder.bind(genre)

        val selected = selectedGenres.contains(genre)
        holder.setSelected(selected)
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
        for (payload in payloads) {
            if (payload === Payloads.ITEM_SELECTED) {
                holder.setSelected(true)
                return
            }
            if (payload === Payloads.ITEM_UNSELECTED) {
                holder.setSelected(false)
                return
            }
        }
        holder.update(getItem(position), payloads)
    }

    fun setItemSelected(position: Int) {
        notifyItemChanged(position, Payloads.ITEM_SELECTED)
    }

    fun setItemUnselected(position: Int) {
        notifyItemChanged(position, Payloads.ITEM_UNSELECTED)
    }

    fun setItemsSelected(selected: Boolean) {
        forEachHolder { holder -> holder.setSelected(selected) }
    }

}