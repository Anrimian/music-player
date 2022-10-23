package com.github.anrimian.musicplayer.ui.utils.views.recycler_view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.github.anrimian.musicplayer.ui.utils.wrappers.DeferredObject2

class SingleItemAdapter<T : ViewBinding>(
    private val viewFetcher: (inflater: LayoutInflater, parent: ViewGroup) -> T
) : RecyclerView.Adapter<SingleItemAdapter<T>.StubViewHolder>() {

    private val viewWrapper = DeferredObject2<T>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): StubViewHolder {
        val viewBinding = viewFetcher(LayoutInflater.from(viewGroup.context), viewGroup)
        return StubViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: StubViewHolder, position: Int) {
        viewWrapper.setObject(holder.viewBinding)
    }

    override fun getItemCount() = 1

    fun runAction(action: (T) -> Unit) {
        viewWrapper.call(action)
    }

    inner class StubViewHolder(val viewBinding: T): RecyclerView.ViewHolder(viewBinding.root)
}
