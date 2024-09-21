package com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop.DragListener

/**
 * Created on 31.10.2017.
 */
class PlayListItemViewHolder(
    inflater: LayoutInflater,
    parent: ViewGroup,
    menuClickListener: (View, Int, PlayListItem) -> Unit,
    onItemClickListener: (PlayListItem, Int) -> Unit,
) : MvpDiffAdapter.MvpViewHolder(inflater.inflate(R.layout.item_storage_music, parent, false)),
    DragListener, SwipeListener {

    private val compositionItemWrapper: CompositionItemWrapper<Composition>

    private lateinit var item: PlayListItem

    private var isCurrent = false

    init {
        compositionItemWrapper = CompositionItemWrapper(
            itemView,
            { onItemClickListener(item, bindingAdapterPosition) },
            { onItemClickListener(item, bindingAdapterPosition) }
        )
        itemView.findViewById<View>(R.id.btnActionsMenu).setOnClickListener { v ->
            menuClickListener(v, bindingAdapterPosition, item)
        }
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

    fun bind(item: PlayListItem, coversEnabled: Boolean) {
        this.item = item
        compositionItemWrapper.bind(item, coversEnabled)
    }

    fun setFileSyncStates(fileSyncStates: Map<Long, FileSyncState>) {
        compositionItemWrapper.showFileSyncState(fileSyncStates[item.id])
    }

    fun showCurrentComposition(
        currentComposition: CurrentComposition?,
        animate: Boolean
    ) {
        var isCurrent = false
        var isPlaying = false
        if (currentComposition != null) {
            isCurrent = item.id == currentComposition.composition?.id
            isPlaying = isCurrent && currentComposition.isPlaying
        }
        showAsCurrentComposition(isCurrent)
        compositionItemWrapper.showAsPlaying(isPlaying, animate)
    }

    private fun showAsCurrentComposition(isCurrent: Boolean) {
        if (this.isCurrent != isCurrent) {
            this.isCurrent = isCurrent
            compositionItemWrapper.showAsCurrentComposition(isCurrent)
        }
    }

}