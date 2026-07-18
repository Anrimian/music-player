package com.github.anrimian.utils.list

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffCallback
import java.util.Collections
import java.util.LinkedList
import kotlin.math.max
import kotlin.math.min

object ListUpdateUtil {

    fun <T> calculateDiff(
        oldList: List<T>,
        newList: List<T>,
        contentCheckFunction: (oldItem: T, newItem: T) -> Boolean,
    ): List<ListUpdateCommand<T>> {
        val result = DiffUtil.calculateDiff(
            SimpleDiffCallback(oldList, newList, contentCheckFunction)
        )
        val updates = LinkedList<ListUpdateCommand<T>>()

        result.dispatchUpdatesTo(object: ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
                println("onInserted: pos:$position, count:$count")
                updates.add(ListUpdateCommand.StubInsert(position, count))
            }

            override fun onRemoved(position: Int, count: Int) {
                println("onRemoved: pos:$position, count:$count")
                val c = count - 1
                val endPos = (position + c).coerceAtMost(oldList.lastIndex)
                for (i in endPos downTo endPos - c) {
                    println("   i: $i")
                    updates.add(ListUpdateCommand.Remove(i))
                }
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                println("onMoved: from:$fromPosition, to:$toPosition")
                updates.add(ListUpdateCommand.Move(fromPosition, toPosition))
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
                println("onChanged: pos:$position, count:$count")
                for (i in position..<position + count) {
                    val newIndex = result.convertOldPositionToNew(i)
                    println("   i: $i, new i: $newIndex, item: ${newList[newIndex]}")
                    updates.add(ListUpdateCommand.Update(i, newList[newIndex]))
                }
//                println("onChanged: pos:$position, count:$count")
//                for (i in position..<position + count) {
//                    println("   i: $i, item: ${newList[i]}")
//                    updates.add(ListUpdateCommand.Update(i, newList[i]))
//                }
            }
        })


        newList.forEachIndexed { index, item ->
            val newIndex = result.convertNewPositionToOld(index)
            if (newIndex == RecyclerView.NO_POSITION) {
                println("Insert: index:$index, item:$item")
                updates.add(ListUpdateCommand.Insert(index, item))
            }
        }
        return updates
    }

    fun <T> applyDiff(list: ArrayList<T>, updates: List<ListUpdateCommand<T>>) {
        println("apply diff")
        updates.forEach { command ->
            when(command) {
                is ListUpdateCommand.StubInsert -> {
                    for (i in command.position..<command.position + command.count) {
                        @Suppress("UNCHECKED_CAST")
                        list.add(i, null as T)
                        println("StubInsert, index: $i")
                    }
                }
                is ListUpdateCommand.Insert -> {
                    list[command.index] = command.item
                    println("Insert, index: ${command.index}, item: ${command.item}")
                }
                is ListUpdateCommand.Remove -> {
                    list.removeAt(command.index)
                    println("Remove, index: ${command.index}")
                }
                is ListUpdateCommand.Move -> {
                    val min = min(command.from, command.to)
                    val max = max(command.from, command.to)
                    for (i in min..max) {
                        if (list[i] == null) {
                            println("Move, swap null: ${i}, to: ${i - 1}")
                            Collections.swap(list, i, i - 1)
                        }
                    }
                    Collections.swap(list, command.from, command.to)
                    println("Move, from: ${command.from}, to: ${command.to}")
                }
                is ListUpdateCommand.Update -> {
                    list[command.index] = command.item
                    println("Update, index: ${command.index}, item: ${command.item}")
                }
            }
            println(list)
        }
    }

}

sealed interface ListUpdateCommand<T> {
    class StubInsert<T>(val position: Int, val count: Int): ListUpdateCommand<T>
    class Insert<T>(val index: Int, val item: T): ListUpdateCommand<T>
    class Remove<T>(val index: Int): ListUpdateCommand<T>
    class Move<T>(val from: Int, val to: Int): ListUpdateCommand<T>
    class Update<T>(val index: Int, val item: T): ListUpdateCommand<T>
}

/** for debug purposes */
fun <T> List<ListUpdateCommand<T>>.toString(itemToString: (T) -> String): String {
    val sb = StringBuilder()
    forEach { command ->
        when(command) {
            is ListUpdateCommand.StubInsert -> {
                sb.append("\nStubInsert: position:${command.position}, count:${command.count}")
            }
            is ListUpdateCommand.Insert -> {
                sb.append("\nInsert: [${command.index}] = ${itemToString(command.item)}")
            }
            is ListUpdateCommand.Remove -> {
                sb.append("\nRemove: [${command.index}]")
            }
            is ListUpdateCommand.Move -> {
                sb.append("\nMove: [${command.from}] = ${command.to}")
            }
            is ListUpdateCommand.Update -> {
                sb.append("\nUpdate: [${command.index}] = ${itemToString(command.item)}")
            }
        }
    }
    return sb.toString()
}