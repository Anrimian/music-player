package com.github.anrimian.musicplayer.wear.ui.queue

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter
import com.github.anrimian.musicplayer.wear.domain.models.PlayQueueItem

class PlayQueueAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
): MvpDiffAdapter<PlayQueueItem, PlayQueueViewHolder>(
    lifecycleOwner,
    recyclerView,
    SimpleDiffItemCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayQueueViewHolder {
        return PlayQueueViewHolder(parent)
    }

    override fun onBindViewHolder(holder: PlayQueueViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bind(getItem(position))
    }

}