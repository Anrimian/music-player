package com.github.anrimian.musicplayer.ui.player_screen.queue.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop.DragListener

/**
 * Created on 31.10.2017.
 */
class PlayQueueViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    onCompositionClickListener: (Int, PlayQueueItem) -> Unit,
    menuClickListener: (View, PlayQueueItem) -> Unit,
    iconClickListener: (Int, PlayQueueItem) -> Unit
) : MvpDiffAdapter.MvpViewHolder(inflater.inflate(R.layout.item_play_queue, parent, false)),
    DragListener, SwipeListener {

    private val compositionItemWrapper: CompositionItemWrapper

    private lateinit var playQueueItem: PlayQueueItem

    init {
        val btnActionsMenu = itemView.findViewById<View>(R.id.btnActionsMenu)
        compositionItemWrapper = CompositionItemWrapper(
            itemView,
            { iconClickListener(bindingAdapterPosition, playQueueItem) }
        ) { onCompositionClickListener(bindingAdapterPosition, playQueueItem) }

        btnActionsMenu.setOnClickListener { v -> menuClickListener(v, playQueueItem) }
    }

    override fun release() {
        compositionItemWrapper.release()
    }

    override fun onDragStateChanged(dragging: Boolean) {
        compositionItemWrapper.showAsDraggingItem(dragging)
    }

    override fun onSwipeStateChanged(swipeOffset: Float) {
        compositionItemWrapper.showAsSwipingItem(swipeOffset)
    }

    fun bind(item: PlayQueueItem, showCovers: Boolean) {
        playQueueItem = item
        val composition = item.composition
        compositionItemWrapper.bind(composition, showCovers)
    }

    fun update(item: PlayQueueItem, payloads: List<*>) {
        playQueueItem = item
        compositionItemWrapper.update(item.composition, payloads)
    }

    fun setCoversVisible(visible: Boolean) {
        compositionItemWrapper.showCompositionImage(visible)
    }

    fun showAsCurrentItem(show: Boolean) {
        compositionItemWrapper.showAsCurrentComposition(show)
    }

    fun showAsPlaying(playing: Boolean, animate: Boolean) {
        compositionItemWrapper.showAsPlaying(playing, animate)
    }

    fun setFileSyncStates(fileSyncStates: Map<Long, FileSyncState>) {
        compositionItemWrapper.showFileSyncState(fileSyncStates[playQueueItem.composition.id])
    }

    fun getPlayQueueItem() = playQueueItem
}