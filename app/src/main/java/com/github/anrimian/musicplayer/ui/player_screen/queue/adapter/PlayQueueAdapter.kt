package com.github.anrimian.musicplayer.ui.player_screen.queue.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.filesync.models.state.file.FileSyncState
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
    SimpleDiffItemCallback(PlayQueueItemHelper::areSourcesTheSame, PlayQueueItemHelper::getChangePayload),
    detectMoves = false//performance optimization, seems queue diff can have too many moves
) {

    private var isListAccessible = true
    private var listDelayedAction: (() -> Unit)? = null

    private var currentItem: PlayQueueItem? = null
    private var play = false
    private var isCoversEnabled = false
    private var syncStates = emptyMap<Long, FileSyncState>()

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
        holder.setFileSyncStates(syncStates)
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

    override fun submitList(list: List<PlayQueueItem>?) {
        isListAccessible = false
        super.submitList(list) {
            listDelayedAction?.invoke()
            listDelayedAction = null
            isListAccessible = true
        }
    }

    /**
     * When submitList() is in progress, scrolling to position is bugged.
     * It doesn't scroll and then RecyclerView wrongly thinks that we're on the target position.
     * So we delay possible action when submitList() is running.
     * Helpful only for single submitList() call case.
     *  + can be solved by using call counter(activeSubmitsCount) instead of boolean flag.
     *  + reproduce problem and implement
     */
    fun runSafeAction(action: () -> Unit) {
        if (isListAccessible) {
            action.invoke()
        } else {
            this.listDelayedAction = action
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

    fun showFileSyncStates(states: Map<Long, FileSyncState>) {
        this.syncStates = states
        forEachHolder { holder ->
            holder.setFileSyncStates(syncStates)
        }
    }
}