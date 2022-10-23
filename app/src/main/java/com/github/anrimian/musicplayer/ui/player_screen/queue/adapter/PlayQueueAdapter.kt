package com.github.anrimian.musicplayer.ui.player_screen.queue.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.utils.PlayQueueItemHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

/**
 * Created on 31.10.2017.
 */
class PlayQueueAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val onCompositionClickListener: (Int, PlayQueueItem) -> Unit,
    private val menuClickListener: (View, PlayQueueItem) -> Unit,
    private val iconClickListener: (Int, PlayQueueItem) -> Unit
) : MvpDiffAdapter<PlayQueueItem, PlayQueueViewHolder>(
    lifecycleOwner,
    recyclerView,
    SimpleDiffItemCallback(PlayQueueItemHelper::areSourcesTheSame, PlayQueueItemHelper::getChangePayload)
) {

    private var currentItem: PlayQueueItem? = null
    private var play = false
    private var isCoversEnabled = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayQueueViewHolder {
        return PlayQueueViewHolder(
            LayoutInflater.from(parent.context),
            parent,
            onCompositionClickListener,
            menuClickListener,
            iconClickListener
        )
    }

    override fun onBindViewHolder(holder: PlayQueueViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val item = getItem(position)
        holder.bind(item, isCoversEnabled)

        val isCurrentItem = item == currentItem
        holder.showAsCurrentItem(isCurrentItem)
        holder.showAsPlaying(isCurrentItem && play, false)
    }

    override fun onBindViewHolder(
        holder: PlayQueueViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            holder.update(getItem(position), payloads)
        }
    }

    fun onCurrentItemChanged(currentItem: PlayQueueItem) {
        this.currentItem = currentItem
        forEachHolder { holder ->
            val isCurrentItem = holder.getPlayQueueItem() == currentItem
            holder.showAsCurrentItem(isCurrentItem)
            holder.showAsPlaying(isCurrentItem && play, true)
        }
    }

    fun showPlaying(play: Boolean) {
        this.play = play
        forEachHolder { holder ->
            val isCurrentItem = holder.getPlayQueueItem() == currentItem
            holder.showAsPlaying(isCurrentItem && play, true)
        }
    }

    fun setCoversEnabled(isCoversEnabled: Boolean) {
        this.isCoversEnabled = isCoversEnabled
        forEachHolder { holder ->
            holder.setCoversVisible(isCoversEnabled)
        }
    }
}