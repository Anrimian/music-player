package com.github.anrimian.utils.wear

import android.content.Context
import com.github.anrimian.musicplayer.domain.utils.rx.RxUtils
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class NodesAvailabilityObservable(
    private val context: Context,
    private val capability: String,
    private val errorCallback: (Throwable) -> Unit
) {

    private val availableNodesSubject = BehaviorSubject.create<Set<Node>>()

    fun getObservable(): Observable<Set<Node>> {
        return RxUtils.withDefaultValue(availableNodesSubject) { getAvailableNodes() }
            .distinctUntilChanged { nodes -> nodes.size }
    }

    fun onCapabilityChanged(info: CapabilityInfo) {
        if (info.name == capability) {
            availableNodesSubject.onNext(info.nodes)
        }
    }

    private fun getAvailableNodes(): Set<Node> {
        return try {
            val capabilityTask = Wearable.getCapabilityClient(context)
                .getCapability(capability, CapabilityClient.FILTER_ALL)
            val capability = Tasks.await(capabilityTask)
            capability.nodes
        } catch (e: Exception) {
            val cause = e.cause
            if (!(cause is ApiException && cause.statusCode == CommonStatusCodes.API_NOT_CONNECTED)) {
                errorCallback(e)
            }
            emptySet()
        }
    }

}