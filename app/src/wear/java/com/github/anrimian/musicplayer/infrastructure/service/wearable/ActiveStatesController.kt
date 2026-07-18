package com.github.anrimian.musicplayer.infrastructure.service.wearable

import android.util.Log
import com.github.anrimian.musicplayer.data.utils.rx.retryWithDelay
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics
import com.github.anrimian.musicplayer.domain.interactors.player.LibraryPlayerInteractor
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueItem
import com.github.anrimian.musicplayer.domain.models.utils.PlayQueueItemHelper
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.utils.list.ListUpdateUtil
import com.google.android.gms.wearable.Node
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.LinkedList
import java.util.concurrent.TimeUnit

class ActiveStatesController(
    private val libraryPlayerInteractor: LibraryPlayerInteractor,
    private val wearableApi: WearableApi,
    private val analytics: Analytics,
    private val ioScheduler: Scheduler,
    private val errorParser: ErrorParser
) {

    private val activeStatesMap = HashMap<String, ActiveStates>()

    fun onAvailableNodesChanged(nodes: Set<Node>) {
        val activeNodeIds = nodes.mapTo(HashSet(), Node::getId)
        val nodesToRemove = LinkedList<String>()
        activeStatesMap.forEach { (nodeId, state) ->
            if (!activeNodeIds.contains(nodeId)) {
                state.release()
                nodesToRemove.add(nodeId)
            }
        }
        nodesToRemove.forEach { nodeId -> activeStatesMap.remove(nodeId) }

/*        //actually no(just for tests), remove later
        activeNodeIds.forEach { nodeId ->
            if (!activeStatesMap.containsKey(nodeId)) {
                onQueueSubscriptionRequested(nodeId, null)
            }
        }*/
    }

    fun onQueueSubscriptionRequested(nodeId: String, updateTime: Long?) {
        val activeStateContainer = activeStatesMap.getOrPut(nodeId, ::ActiveStates)

        val listData = activeStateContainer.getQueueListState()
        if (listData != null) {
            if (listData.updateTime != updateTime) {
                //can be not only time?
                //++? (but respect position from node, if any(send last-end item ids+queueId+queueUpdateTime)
                wearableApi.sendPlayQueue(nodeId, listData)
            }
            return
        }
        subscribeOnQueue(nodeId, activeStateContainer, WindowListData(INITIAL_START_OFFSET, INITIAL_END_OFFSET))
    }

    fun onQueueExpandRequested(
        nodeId: String,
        isForward: Boolean,
        borderItemId: Long,
        currentItemsCount: Int,
        expandSize: Int,
        lastReceivedUpdateTime: Long
    ) {
        val activeStates = activeStatesMap.getOrPut(nodeId, ::ActiveStates)
        val listData = activeStates.getQueueListState()
        if (listData == null) {
            // covers case: open queue on wear, scroll down, close phone app, open wear, scroll more
            //TODO-W check, +check for small lists
            val endOffset = if (currentItemsCount > INITIAL_START_OFFSET + INITIAL_END_OFFSET) {
                currentItemsCount - INITIAL_START_OFFSET
            } else {
                INITIAL_END_OFFSET
            }
            val windowListData = WindowListData<PlayQueueItem>(INITIAL_START_OFFSET, endOffset)
            subscribeOnQueue(nodeId, activeStates, windowListData)
            return
        }
        //check for list consistency
        if (listData.updateTime != lastReceivedUpdateTime
            || (isForward && listData.last().id != borderItemId)
            || !isForward && listData.first().id != borderItemId) {
            wearableApi.sendPlayQueue(nodeId, listData)
            return
        }

        if (isForward) {
            listData.endOffset += listData.endOffset + expandSize
        } else {
            listData.startOffset += listData.startOffset + expandSize
        }
        activeStates.releaseQueue()
        subscribeOnQueue(nodeId, activeStates, listData)
    }

    fun onQueueSubscriptionCancel(nodeId: String) {
        activeStatesMap.remove(nodeId)?.release()
    }

    private fun subscribeOnQueue(
        nodeId: String,
        activeStates: ActiveStates,
        listData: WindowListData<PlayQueueItem>
    ) {
        activeStates.setQueueListState(listData)

        val disposable = libraryPlayerInteractor.getWindowPlayQueueObservable(
            listData.startOffset,
            listData.endOffset
        ).observeOn(ioScheduler)
            .retryWithDelay(5, 3, TimeUnit.SECONDS)
            .subscribe(
                { queue -> onPlayQueueReceived(nodeId, listData, queue) },
                { t -> onPlayQueueErrorReceived(nodeId, activeStates, t)}
            )

        activeStates.setQueueDisposable(disposable)
    }

    //OLD
    //we always get new queue on item change - do not observe item id change, request by stable id?
    // on receive: compare and send only new items?
    //we get less items when queue is reached to the end
    // A: compare result size after event, if less and queue size is more - request before/after
    private var testList = ArrayList<PlayQueueItem>()
    private fun onPlayQueueReceived(
        nodeId: String,
        listData: WindowListData<PlayQueueItem>,
        newList: List<PlayQueueItem>,
    ) {
        Log.d("KEK", "onPlayQueueReceived: ${newList.joinToString(prefix = "\n", separator = "\n", transform = { "${it.title},${it.itemId}" })}")

        val oldList = listData.list
        if (oldList.isEmpty() || newList.isEmpty()) {
            listData.list = newList
            listData.updateTime = System.currentTimeMillis()
            wearableApi.sendPlayQueue(nodeId, listData)
            testList = ArrayList(newList)
            return
        }

        Log.d("KEK", "calculateDiff")
        val updateMessage = ListUpdateUtil.calculateDiff(
            oldList,
            newList,
            PlayQueueItemHelper::areSourcesTheSame
        )
        if (updateMessage.isEmpty()) {
            return
        }
        listData.list = newList

        // testing block
        ListUpdateUtil.applyDiff(testList, updateMessage)
        if (newList != testList) {
            val msg = """
                LISTS ARE DIFFERENT, EXPECTED: ${newList.joinToString(prefix = "\n", separator = "\n", transform = { "${it.title},${it.itemId}" })}
                ACTUAL: ${testList.joinToString(prefix = "\n", separator = "\n", transform = { "${it.title},${it.itemId}" })}
            """
            Log.e("KEK", msg)
            analytics.logMessage(msg)
            throw Exception()
        }
        // !testing block

        wearableApi.sendPlayQueueUpdate(nodeId, updateMessage, listData)
    }

    private fun onPlayQueueErrorReceived(
        nodeId: String,
        activeStateContainer: ActiveStates,
        throwable: Throwable
    ) {
        activeStateContainer.releaseQueue()

        analytics.processNonFatalError(throwable)
        val message = errorParser.parseError(throwable).message
        wearableApi.sendErrorMessage(message, nodeId)
    }

    private companion object {
        const val INITIAL_START_OFFSET = 2
        const val INITIAL_END_OFFSET = 7
    }

    private class ActiveStates {

        private val statesDisposable = CompositeDisposable()

        private var queueListState: WindowListData<PlayQueueItem>? = null
        private var queueDisposable: Disposable? = null

        fun getQueueListState() = queueListState

        fun setQueueListState(queueListState: WindowListData<PlayQueueItem>) {
            this.queueListState = queueListState
        }

        fun setQueueDisposable(disposable: Disposable) {
            queueDisposable = disposable
            statesDisposable.add(disposable)
        }

        fun releaseQueue() {
            queueDisposable?.dispose()
            queueListState = null
        }

        fun release() {
            statesDisposable.dispose()
            queueListState = null
        }

    }

}
