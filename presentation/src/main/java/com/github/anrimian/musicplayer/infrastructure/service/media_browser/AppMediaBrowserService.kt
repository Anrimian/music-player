package com.github.anrimian.musicplayer.infrastructure.service.media_browser

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.Constants.TRIGGER
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatAuthor
import com.github.anrimian.musicplayer.utils.Permissions
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable


const val PERMISSION_ERROR_ACTION_ID = "permission_error_action_id"
const val DEFAULT_ERROR_ACTION_ID = "default_error_action_id"
const val RESUME_ACTION_ID = "resume_action_id"
const val SHUFFLE_ALL_AND_PLAY_ACTION_ID = "shuffle_all_and_play_action_id"
const val COMPOSITIONS_ACTION_ID = "compositions_action_id"
const val FOLDERS_ACTION_ID = "folders_action_id"

const val POSITION_ARG = "position_arg"
const val COMPOSITION_ID_ARG = "composition_id_arg"
const val FOLDER_ID_ARG = "folder_id_arg"

const val ROOT_FOLDER = 0L

private const val ROOT_ID = "root_id"

private const val COMPOSITIONS_NODE_ID = "compositions_node_id"
private const val FOLDERS_NODE_ID = "folders_node_id"
private const val ARTISTS_NODE_ID = "artists_node_id"
private const val ALBUMS_NODE_ID = "albums_node_id"

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

    //exists also options(overloaded method), and there are:
    //var page: Int = options.getInt(MediaBrowserCompat.EXTRA_PAGE)
    //var pageSize: Int = options.getInt(MediaBrowserCompat.EXTRA_PAGE_SIZE)
    override fun onLoadChildren(
        parentId: String,
        resultCallback: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            ROOT_ID -> loadRootItems(resultCallback)
            COMPOSITIONS_NODE_ID -> loadCompositionItems(resultCallback)
            FOLDERS_NODE_ID -> loadFolderItems(resultCallback, ROOT_FOLDER)
            else -> resultCallback.sendResult(emptyList())
        }
    }

    //never called
    override fun onLoadChildren(
        parentId: String,
        resultCallback: Result<List<MediaBrowserCompat.MediaItem>>,
        options: Bundle
    ) {
        when (parentId) {
            ROOT_ID -> loadRootItems(resultCallback)
            COMPOSITIONS_NODE_ID -> loadCompositionItems(resultCallback)
            FOLDERS_NODE_ID -> loadFolderItems(resultCallback, options.getLong(FOLDER_ID_ARG))
            else -> resultCallback.sendResult(emptyList())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentRequestDisposable?.dispose()
        itemUpdateDisposableMap.forEach { entry -> entry.value.dispose() }
        Components.getAppComponent().mediaSessionHandler().dispatchServiceDestroyed()
    }

    //use only alphabetical order?
    private fun loadCompositionItems(resultCallback: Result<List<MediaBrowserCompat.MediaItem>>) {
        loadItems(
            COMPOSITIONS_NODE_ID,
            resultCallback,
            Components.getAppComponent().musicServiceInteractor().compositionsObservable
        ) { compositions -> compositions.mapIndexed(this::toActionItem) }
    }

    private fun loadFolderItems(
        resultCallback: Result<List<MediaBrowserCompat.MediaItem>>,
        folderId: Long
    ) {
        val idOpt = if (folderId == ROOT_FOLDER) null else folderId
        loadItems(
            FOLDERS_NODE_ID,
            resultCallback,
            Components.getAppComponent().musicServiceInteractor().getFoldersObservable(idOpt)
        ) { sources -> sources.map { source -> toActionItem(source, folderId) } }
    }

    private fun loadRootItems(resultCallback: Result<List<MediaBrowserCompat.MediaItem>>) {
        val observable = Components.getAppComponent()
            .libraryPlayerInteractor()
            .playQueueSizeObservable
            .map { size -> size > 0 }

        loadItems(
            ROOT_ID,
            resultCallback,
            observable
        ) { isPlayQueueExists ->
            return@loadItems arrayListOf<MediaBrowserCompat.MediaItem>().apply {
                if (isPlayQueueExists) {
                    add(actionItem(RESUME_ACTION_ID, R.string.resume))
                }
                add(actionItem(SHUFFLE_ALL_AND_PLAY_ACTION_ID, R.string.shuffle_all_and_play))
                add(browsableItem(COMPOSITIONS_NODE_ID, R.string.compositions))
                add(browsableItem(FOLDERS_NODE_ID, R.string.folders))
                add(browsableItem(ARTISTS_NODE_ID, R.string.artists))
                add(browsableItem(ALBUMS_NODE_ID, R.string.albums))
            }
        }
    }

    private fun <T> loadItems(rootItemId: String,
                              resultCallback: Result<List<MediaBrowserCompat.MediaItem>>,
                              valuesObservable: Observable<T>,
                              resultMapper: (T) -> List<MediaBrowserCompat.MediaItem>
    ) {
        if (!Permissions.hasFilePermission(this)) {
            resultCallback.sendErrorResult(PERMISSION_ERROR_ACTION_ID, R.string.no_file_permission)
            return
        }

        val observable = valuesObservable
            .replay(1)
            .refCount()

        currentRequestDisposable = observable
            .firstOrError()
            .subscribe(
                { value ->
                    resultCallback.sendResult(resultMapper(value))

                    registerBrowsableItemUpdate(rootItemId, observable)
                },
                { throwable ->
                    val errorParser = Components.getAppComponent().errorParser()
                    errorParser.logError(throwable)
                    val errorCommand = errorParser.parseError(throwable)
                    resultCallback.sendErrorResult(DEFAULT_ERROR_ACTION_ID, errorCommand.message)

                    registerBrowsableItemUpdate(rootItemId, observable)
                })

        resultCallback.detach()
    }

    private fun registerBrowsableItemUpdate(itemId: String, observable: Observable<*>) {
        if (itemUpdateDisposableMap.containsKey(itemId)) {
            return
        }
        val disposable = observable.distinctUntilChanged()
            .skip(1)
            .map { TRIGGER }
            .onErrorReturn(this::processBrowsableItemUpdateError)
            .subscribe { notifyChildrenChanged(itemId) }
        itemUpdateDisposableMap[itemId] = disposable
    }

    private fun processBrowsableItemUpdateError(throwable: Throwable): Any {
        Components.getAppComponent().analytics().processNonFatalError(throwable)
        return TRIGGER
    }

    private fun Result<List<MediaBrowserCompat.MediaItem>>.sendErrorResult(
        mediaId: String,
        titleResId: Int
    ) {
        sendErrorResult(mediaId, getString(titleResId))
    }

    private fun Result<List<MediaBrowserCompat.MediaItem>>.sendErrorResult(
        mediaId: String,
        message: String
    ) {
        sendResult(listOf(actionItem(mediaId, message)))
    }

    private fun toActionItem(position: Int, composition: Composition) = actionItem(
        COMPOSITIONS_ACTION_ID,
        formatCompositionName(composition),
        formatAuthor(composition.artist, this),
        Bundle().apply { putInt(POSITION_ARG, position) }
    )

    private fun toActionItem(fileSource: FileSource, folderId: Long): MediaBrowserCompat.MediaItem {
        return when(fileSource) {
            is FolderFileSource -> {
                browsableItem(
                    FOLDERS_NODE_ID,
                    fileSource.name,
                    FormatUtils.formatCompositionsCount(this, fileSource.filesCount),
                    Bundle().apply { putLong(FOLDER_ID_ARG, folderId)}
                )
            }
            is CompositionFileSource -> {
                val composition = fileSource.composition
                actionItem(
                    FOLDERS_ACTION_ID,
                    formatCompositionName(composition),
                    formatAuthor(composition.artist, this),
                    Bundle().apply {
                        putLong(COMPOSITION_ID_ARG, composition.id)
                        putLong(POSITION_ARG, folderId)
                    }
                )
            }
            else -> throw IllegalStateException()
        }
    }

    private fun actionItem(mediaId: String, titleResId: Int, subtitle: CharSequence? = null) =
        actionItem(mediaId, getString(titleResId), subtitle)

    private fun actionItem(mediaId: String,
                           title: CharSequence?,
                           subtitle: CharSequence? = null,
                           extras: Bundle? = null
    ) = MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setTitle(title)
            .setMediaId(mediaId)
            .setSubtitle(subtitle)
            .setExtras(extras)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
    )

    private fun browsableItem(mediaId: String, titleResId: Int) =
        browsableItem(mediaId, getString(titleResId))

    private fun browsableItem(mediaId: String,
                              title: CharSequence?,
                              subtitle: CharSequence? = null,
                              extras: Bundle? = null
    ) = MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setTitle(title)
            .setMediaId(mediaId)
            .setSubtitle(subtitle)
            .setExtras(extras)
            .build(),
        MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
    )

}