package com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.domain.models.utils.PlayListItemHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

/**
 * Created on 31.10.2017.
 */
class PlayListItemAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val coversEnabled: Boolean,
    private val menuClickListener: (View, Int, PlayListItem) -> Unit,
    private val onIconClickListener: (Int) -> Unit
) : MvpDiffAdapter<PlayListItem, PlayListItemViewHolder>(
    lifecycleOwner,
    recyclerView,
    SimpleDiffItemCallback(PlayListItemHelper::areSourcesTheSame, PlayListItemHelper::getChangePayload)
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListItemViewHolder {
        return PlayListItemViewHolder(
            LayoutInflater.from(parent.context),
            parent,
            menuClickListener,
            onIconClickListener
        )
    }

    override fun onBindViewHolder(holder: PlayListItemViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val composition = getItem(position)
        holder.bind(composition, coversEnabled)
    }
}