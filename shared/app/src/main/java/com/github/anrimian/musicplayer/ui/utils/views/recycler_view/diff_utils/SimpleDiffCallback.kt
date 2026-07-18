package com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils

import androidx.recyclerview.widget.DiffUtil

/**
 * Created on 17.02.2018.
 */
class SimpleDiffCallback<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
    private val contentCheckFunction: (oldItem: T, newItem: T) -> Boolean = getEqualContentCheckFunction(),
    private val payloadFunction: (oldItem: T, newItem: T) -> List<Any>? = getPayloadDefaultFunction()
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return contentCheckFunction(oldItem, newItem)
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return payloadFunction(oldItem, newItem)
    }

    private companion object {

        fun <T> getEqualContentCheckFunction() = { oldItem: T, newItem: T -> oldItem == newItem }

        fun <T> getPayloadDefaultFunction() = { oldItem: T, newItem: T -> null }

    }

}
