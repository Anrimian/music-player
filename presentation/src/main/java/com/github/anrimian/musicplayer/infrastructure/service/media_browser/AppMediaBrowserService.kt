package com.github.anrimian.musicplayer.infrastructure.service.media_browser

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

const val RESUME_ACTION_ID = "resume_action_id"
const val SHUFFLE_ALL_AND_PLAY_ACTION_ID = "shuffle_all_and_play_action_id"

private const val ROOT_ID = "root_id"

private const val COMPOSITIONS_NODE_ID = "compositions_node_id"
private const val FOLDERS_NODE_ID = "folders_node_id"
private const val ARTISTS_NODE_ID = "artists_node_id"
private const val ALBUMS_NODE_ID = "albums_node_id"

//handle permissions
//handle android 11 EXTRA_RECENT
//media state is not cleared on stop
class AppMediaBrowserService: MediaBrowserServiceCompat() {

    private val itemUpdateDisposableMap = HashMap<String, Disposable>()
    private var currentRequestDisposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()
        val mediaSession = Components.getAppComponent()
            .mediaSessionHandler()
            .getMediaSession()
        this.sessionToken = mediaSession.sessionToken
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        resultCallback: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == ROOT_ID) {
            loadRootItems(resultCallback)
            return
        }
        resultCallback.sendResult(emptyList())
    }

    override fun onDestroy() {
        super.onDestroy()
        currentRequestDisposable?.dispose()
        itemUpdateDisposableMap.forEach { entry -> entry.value.dispose() }
    }

    //handle errors
    //increase loading speed
    //fun can be templated
    private fun loadRootItems(resultCallback: Result<List<MediaBrowserCompat.MediaItem>>) {
        val observable = Components.getAppComponent()
            .libraryPlayerInteractor()
            .playQueueSizeObservable
            .map { size -> size > 0 }

        currentRequestDisposable = observable
            .firstOrError()
            .subscribe { isPlayQueueExists ->
                val mediaItems = arrayListOf<MediaBrowserCompat.MediaItem>()
                if (isPlayQueueExists) {
                    mediaItems.add(actionItem(R.string.resume, RESUME_ACTION_ID))
                }
                mediaItems.apply {
                    add(actionItem(R.string.shuffle_all_and_play, SHUFFLE_ALL_AND_PLAY_ACTION_ID))
                    add(browsableItem(R.string.compositions, COMPOSITIONS_NODE_ID))
                    add(browsableItem(R.string.folders, FOLDERS_NODE_ID))
                    add(browsableItem(R.string.artists, ARTISTS_NODE_ID))
                    add(browsableItem(R.string.albums, ALBUMS_NODE_ID))
                }
                resultCallback.sendResult(mediaItems)

                registerBrowsableItemUpdate(observable, ROOT_ID)
            }

        resultCallback.detach()
    }

    private fun registerBrowsableItemUpdate(observable: Observable<*>, itemId: String) {
        if (itemUpdateDisposableMap.containsKey(itemId)) {
            return
        }
        val disposable = observable.distinctUntilChanged()
            .onErrorComplete()//log errors?
            .subscribe { notifyChildrenChanged(itemId) }
        itemUpdateDisposableMap[itemId] = disposable
    }

    private fun actionItem(titleResId: Int, mediaId: String) =
        actionItem(getString(titleResId), mediaId)

    private fun actionItem(title: CharSequence?, mediaId: String) = MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setTitle(title)
            .setMediaId(mediaId)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
    )

    private fun browsableItem(titleResId: Int, mediaId: String) =
        browsableItem(getString(titleResId), mediaId)

    private fun browsableItem(title: CharSequence?, mediaId: String) = MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setTitle(title)
            .setMediaId(mediaId)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
    )

}