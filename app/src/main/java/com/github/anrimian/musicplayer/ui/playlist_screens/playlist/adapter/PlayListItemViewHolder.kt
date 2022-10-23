package com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
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
    onIconClickListener: (Int) -> Unit,
) : MvpDiffAdapter.MvpViewHolder(inflater.inflate(R.layout.item_storage_music, parent, false)),
    DragListener, SwipeListener {

    private val compositionItemWrapper: CompositionItemWrapper

    private lateinit var item: PlayListItem

    init {
        compositionItemWrapper = CompositionItemWrapper(
            itemView,
            { onIconClickListener(bindingAdapterPosition) }
        ) { onIconClickListener(bindingAdapterPosition) }
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
        val composition = item.composition
        compositionItemWrapper.bind(composition, coversEnabled)
    }
}