package com.github.anrimian.musicplayer.infrastructure.service.media_browser

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.utils.Permissions
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

const val REQUEST_FILE_PERMISSION_ACTION_ID = "request_file_permission_action_id"
const val RESUME_ACTION_ID = "resume_action_id"
const val SHUFFLE_ALL_AND_PLAY_ACTION_ID = "shuffle_all_and_play_action_id"

private const val ROOT_ID = "root_id"

private const val COMPOSITIONS_NODE_ID = "compositions_node_id"
private const val FOLDERS_NODE_ID = "folders_node_id"
private const val ARTISTS_NODE_ID = "artists_node_id"
private const val ALBUMS_NODE_ID = "albums_node_id"

//permission error state
//handle android 11 EXTRA_RECENT

//support navigation hints

//checklist:
//how it will work with external player?
class AppMediaBrowserService: MediaBrowserServiceCompat() {

    private val itemUpdateDisposableMap = HashMap<String, Disposable>()
    private var currentRequestDisposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()
        val mediaSessionHandler = Components.getAppComponent().mediaSessionHandler()
        mediaSessionHandler.dispatchServiceCreated()
        this.sessionToken = mediaSessionHandler.getMediaSession().sessionToken
    }

    /*
      val maximumRootChildLimit = rootHints.getInt(
          MediaConstants.BROWSER_ROOT_HINTS_KEY_ROOT_CHILDREN_LIMIT,
          /* defaultValue= */ 4)
      val supportedRootChildFlags = rootHints.getInt(
          MediaConstants.BROWSER_ROOT_HINTS_KEY_ROOT_CHILDREN_SUPPORTED_FLAGS,
          /* defaultValue= */ MediaItem.FLAG_BROWSABLE)
      https://developer.android.google.cn/training/cars/media?hl=en-au

      Note: In Android Auto, only the car screen interface will show navigational tabs. Therefore, these root hints will only be sent when connecting to Android Auto for the car screen, and will not be sent when connecting to Android Auto for the phone screen.
      Caution: Not all versions of Android Automotive OS will send these root hints. In the absence of these hints, you should assume that Android Automotive OS requires only root browsable items, and at most four of them. Take note of the default values in the code snippet above.

     */
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
        Components.getAppComponent().mediaSessionHandler().dispatchServiceDestroyed()
    }

    //reload after permission grant?
    //handle errors
    //increase loading speed
    //fun can be templated
    private fun loadRootItems(resultCallback: Result<List<MediaBrowserCompat.MediaItem>>) {
        if (!Permissions.hasFilePermission(this)) {
            resultCallback.sendResult(listOf(
                actionItem(REQUEST_FILE_PERMISSION_ACTION_ID, R.string.no_file_permission)
            ))
            return
        }

        val observable = Components.getAppComponent()
            .libraryPlayerInteractor()
            .playQueueSizeObservable
            .map { size -> size > 0 }

        currentRequestDisposable = observable
            .firstOrError()
            .subscribe { isPlayQueueExists ->
                val mediaItems = arrayListOf<MediaBrowserCompat.MediaItem>()
                if (isPlayQueueExists) {
                    mediaItems.add(actionItem(RESUME_ACTION_ID, R.string.resume))
                }
                mediaItems.apply {
                    add(actionItem(SHUFFLE_ALL_AND_PLAY_ACTION_ID, R.string.shuffle_all_and_play))
                    add(browsableItem(COMPOSITIONS_NODE_ID, R.string.compositions))
                    add(browsableItem(FOLDERS_NODE_ID, R.string.folders))
                    add(browsableItem(ARTISTS_NODE_ID, R.string.artists))
                    add(browsableItem(ALBUMS_NODE_ID, R.string.albums))
                }
                resultCallback.sendResult(mediaItems)

                registerBrowsableItemUpdate(ROOT_ID, observable)
            }

        resultCallback.detach()
    }

    private fun registerBrowsableItemUpdate(itemId: String, observable: Observable<*>) {
        if (itemUpdateDisposableMap.containsKey(itemId)) {
            return
        }
        val disposable = observable.distinctUntilChanged()
            .onErrorComplete()//log errors?
            .subscribe { notifyChildrenChanged(itemId) }
        itemUpdateDisposableMap[itemId] = disposable
    }

    private fun actionItem(mediaId: String, titleResId: Int, subtitle: CharSequence? = null) =
        actionItem(mediaId, getString(titleResId), subtitle)

    private fun actionItem(mediaId: String,
                           title: CharSequence?,
                           subtitle: CharSequence? = null
    ) = MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setTitle(title)
            .setMediaId(mediaId)
            .setSubtitle(subtitle)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
    )

    private fun browsableItem(mediaId: String, titleResId: Int) =
        browsableItem(mediaId, getString(titleResId))

    private fun browsableItem(mediaId: String, title: CharSequence?) = MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setTitle(title)
            .setMediaId(mediaId)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
    )

}