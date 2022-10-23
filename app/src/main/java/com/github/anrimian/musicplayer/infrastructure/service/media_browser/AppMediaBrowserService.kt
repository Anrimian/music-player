package com.github.anrimian.musicplayer.infrastructure.service.media_browser

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.utils.MediaConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.Permissions
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.Constants.TRIGGER
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.artist.Artist
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource
import com.github.anrimian.musicplayer.domain.models.folders.FileSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.play_queue.PlayQueueEvent
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable


const val PERMISSION_ERROR_ACTION_ID = "permission_error_action_id"
const val DEFAULT_ERROR_ACTION_ID = "default_error_action_id"
const val RECENT_MEDIA_ACTION_ID = "recent_media_action_id"
const val RESUME_ACTION_ID = "resume_action_id"
const val PAUSE_ACTION_ID = "pause_action_id"
const val SHUFFLE_ALL_AND_PLAY_ACTION_ID = "shuffle_all_and_play_action_id"
const val COMPOSITIONS_ACTION_ID = "compositions_action_id"
const val FOLDERS_ACTION_ID = "folders_action_id"
const val ARTIST_ITEMS_ACTION_ID = "artist_items_action_id"
const val ALBUM_ITEMS_ACTION_ID = "album_items_action_id"
const val PLAYLIST_ITEMS_ACTION_ID = "playlist_items_action_id"
const val SEARCH_ITEMS_ACTION_ID = "search_items_action_id"

const val POSITION_ARG = "position_arg"
const val COMPOSITION_ID_ARG = "composition_id_arg"
const val FOLDER_ID_ARG = "folder_id_arg"
const val ARTIST_ID_ARG = "artist_id_arg"
const val ALBUM_ID_ARG = "artist_id_arg"
const val PLAYLIST_ID_ARG = "artist_id_arg"
const val SEARCH_QUERY_ARG = "search_query_arg"

private const val ROOT_ID = "root_id"
private const val RECENT_MEDIA_ROOT_ID = "recent_media_root_id"

private const val COMPOSITIONS_NODE_ID = "compositions_node_id"
private const val FOLDERS_NODE_ID = "folders_node_id"
private const val ARTISTS_NODE_ID = "artists_node_id"
private const val ALBUMS_NODE_ID = "albums_node_id"
private const val PLAYLISTS_NODE_ID = "playlists_node_id"
private const val ARTIST_ITEMS_NODE_ID = "artist_items_node_id"
private const val ALBUM_ITEMS_NODE_ID = "album_items_node_id"
private const val PLAYLIST_ITEMS_NODE_ID = "playlist_items_node_id"

const val DELIMITER = '-'

//strange initial state(random? just in case of install while android auto is active?)

//later improvements:
//for automotive - add images to items
//default image
//remove skip to next when it is not enabled? https://stackoverflow.com/a/45698216/5541688
class AppMediaBrowserService: MediaBrowserServiceCompat() {

    private val itemUpdateDisposableMap = HashMap<String, Disposable>()
    private var currentRequestDisposable: Disposable? = null

    override fun onCreate() {
        super.onCreate()
        val mediaSessionHandler = Components.getAppComponent().mediaSessionHandler()
        mediaSessionHandler.dispatchServiceCreated()
        this.sessionToken = mediaSessionHandler.getMediaSession().sessionToken
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        val extras = Bundle()
        if (rootHints?.getBoolean(BrowserRoot.EXTRA_RECENT) == true) {
            extras.putBoolean(BrowserRoot.EXTRA_RECENT, true)
            return BrowserRoot(RECENT_MEDIA_ROOT_ID, extras)
        }
        extras.putBoolean(MediaConstants.BROWSER_SERVICE_EXTRAS_KEY_SEARCH_SUPPORTED, true)
        return BrowserRoot(ROOT_ID, extras)
    }

    override fun onLoadChildren(
        parentId: String,
        resultCallback: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        when {
            parentId == RECENT_MEDIA_ROOT_ID -> loadRecentItem(resultCallback)
            parentId == ROOT_ID -> loadRootItems(resultCallback)
            parentId == COMPOSITIONS_NODE_ID -> loadCompositionItems(resultCallback)
            parentId.startsWith(FOLDERS_NODE_ID) -> loadFolderItems(resultCallback, parentId)
            parentId == ARTISTS_NODE_ID -> loadArtists(resultCallback)
            parentId.startsWith(ARTIST_ITEMS_NODE_ID) -> loadArtistItems(resultCallback, parentId)
            parentId == ALBUMS_NODE_ID -> loadAlbums(resultCallback)
            parentId.startsWith(ALBUM_ITEMS_NODE_ID) -> loadAlbumItems(resultCallback, parentId)
            parentId == PLAYLISTS_NODE_ID -> loadPlaylists(resultCallback)
            parentId.startsWith(PLAYLIST_ITEMS_NODE_ID) -> loadPlaylistItems(resultCallback, parentId)
            else -> resultCallback.sendResult(emptyList())
        }
    }

    override fun onSearch(
        query: String,
        extras: Bundle?,
        resultCallback: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        if (!Permissions.hasFilePermission(this)) {
            resultCallback.sendErrorResult(PERMISSION_ERROR_ACTION_ID, R.string.no_file_permission)
            return
        }

        resultCallback.detach()

        currentRequestDisposable = Components.getAppComponent()
            .musicServiceInteractor()
            .getCompositionsObservable(query)
            .firstOrError()
            .subscribe(
                { value -> resultCallback.sendResult(value.mapIndexed { position, composition ->
                    toSearchActionItem(position, composition, query)
                }) },
                { throwable -> resultCallback.sendErrorResult(throwable) }
            )
    }

    override fun onDestroy() {
        super.onDestroy()
        currentRequestDisposable?.dispose()
        itemUpdateDisposableMap.forEach { entry -> entry.value.dispose() }
        Components.getAppComponent().mediaSessionHandler().dispatchServiceDestroyed()
    }

    private fun loadRecentItem(resultCallback: Result<List<MediaBrowserCompat.MediaItem>>) {
        resultCallback.detach()
        currentRequestDisposable = Components.getAppComponent()
            .libraryPlayerInteractor()
            .getCurrentQueueItemObservable()
            .firstOrError()
            .flatMap(this::toRecentItem)
            .onErrorReturn(this::processRecentItemError)
            .subscribe(resultCallback::sendResult)
    }

    //use only alphabetical order?
    private fun loadCompositionItems(resultCallback: Result<List<MediaBrowserCompat.MediaItem>>) {
        loadItems(
            COMPOSITIONS_NODE_ID,
            resultCallback,
            Components.getAppComponent().musicServiceInteractor().getCompositionsObservable(null)
        ) { compositions -> compositions.mapIndexed(this::toActionItem) }
    }

    private fun loadFolderItems(
        resultCallback: Result<List<MediaBrowserCompat.MediaItem>>,
        folderNodeId: String
    ) {
        val parentFolderId = folderNodeId.split(DELIMITER).last().toLongOrNull()
        loadItems(
            folderNodeId,
            resultCallback,
            Components.getAppComponent().musicServiceInteractor().getFoldersObservable(parentFolderId)
        ) { sources -> sources.map { source -> toActionItem(source, parentFolderId) } }
    }

    private fun loadArtists(resultCallback: Result<List<MediaBrowserCompat.MediaItem>>) {
        loadItems(
            ARTISTS_NODE_ID,
            resultCallback,
            Components.getAppComponent().musicServiceInteractor().artistsObservable
        ) { sources -> sources.map(this::toBrowsableItem) }
    }

    private fun loadArtistItems(
        resultCallback: Result<List<MediaBrowserCompat.MediaItem>>,
        nodeId: String
    ) {
        val artistId = nodeId.split(DELIMITER).last().toLong()
        loadItems(
            nodeId,
            resultCallback,
            Components.getAppComponent().musicServiceInteractor().getCompositionsByArtist(artistId)
        ) { sources -> sources.mapIndexed { position, composition ->
            toActionArtistItem(position, composition, artistId)
        }
        }
    }

    private fun loadAlbums(resultCallback: Result<List<MediaBrowserCompat.MediaItem>>) {
        loadItems(
            ALBUMS_NODE_ID,
            resultCallback,
            Components.getAppComponent().musicServiceInteractor().albumsObservable
        ) { sources -> sources.map(this::toBrowsableItem) }
    }

    private fun loadAlbumItems(
        resultCallback: Result<List<MediaBrowserCompat.MediaItem>>,
        nodeId: String
    ) {
        val albumId = nodeId.split(DELIMITER).last().toLong()
        loadItems(
            nodeId,
            resultCallback,
            Components.getAppComponent().musicServiceInteractor().getAlbumItemsObservable(albumId)
        ) { sources -> sources.mapIndexed { position, composition ->
            toActionAlbumItem(position, composition, albumId)
        }
        }
    }

    private fun loadPlaylists(resultCallback: Result<List<MediaBrowserCompat.MediaItem>>) {
        loadItems(
            PLAYLISTS_NODE_ID,
            resultCallback,
            Components.getAppComponent().musicServiceInteractor().playListsObservable
        ) { sources -> sources.map(this::toBrowsableItem) }
    }

    private fun loadPlaylistItems(
        resultCallback: Result<List<MediaBrowserCompat.MediaItem>>,
        nodeId: String
    ) {
        val playlistId = nodeId.split(DELIMITER).last().toLong()
        loadItems(
            nodeId,
            resultCallback,
            Components.getAppComponent().musicServiceInteractor().getPlaylistItemsObservable(playlistId)
        ) { sources -> sources.mapIndexed { position, composition ->
            toActionPlaylistItem(position, composition, playlistId)
        }
        }
    }

    private fun loadRootItems(resultCallback: Result<List<MediaBrowserCompat.MediaItem>>) {
        val libraryPlayerInteractor = Components.getAppComponent().libraryPlayerInteractor()
        val observable = Observable.combineLatest(
            libraryPlayerInteractor.getPlayQueueSizeObservable(),
            libraryPlayerInteractor.getIsPlayingStateObservable(),
            ::Pair
        )

        loadItems(
            ROOT_ID,
            resultCallback,
            observable
        ) { (playQueueSize, isPlaying) ->
            return@loadItems arrayListOf<MediaBrowserCompat.MediaItem>().apply {
                if (playQueueSize > 0) {
                    val item = if (isPlaying) {
                        actionItem(PAUSE_ACTION_ID, R.string.pause)
                    } else {
                        actionItem(RESUME_ACTION_ID, R.string.resume)
                    }
                    add(item)
                }
                add(actionItem(SHUFFLE_ALL_AND_PLAY_ACTION_ID, R.string.shuffle_all_and_play))
                add(browsableItem(COMPOSITIONS_NODE_ID, R.string.compositions))
                add(browsableItem(FOLDERS_NODE_ID, R.string.folders))
                add(browsableItem(PLAYLISTS_NODE_ID, R.string.play_lists))
                add(browsableItem(ARTISTS_NODE_ID, R.string.artists))
                add(browsableItem(ALBUMS_NODE_ID, R.string.albums))
            }
        }
    }

    private fun <T: Any> loadItems(rootItemId: String,
                              resultCallback: Result<List<MediaBrowserCompat.MediaItem>>,
                              valuesObservable: Observable<T>,
                              resultMapper: (T) -> List<MediaBrowserCompat.MediaItem>
    ) {
        if (!Permissions.hasFilePermission(this)) {
            resultCallback.sendErrorResult(PERMISSION_ERROR_ACTION_ID, R.string.no_file_permission)
            return
        }

        resultCallback.detach()

        val observable = valuesObservable
            .replay(1)
            .refCount()
            .observeOn(AndroidSchedulers.mainThread())

        currentRequestDisposable = observable
            .firstOrError()
            .subscribe(
                { value ->
                    resultCallback.sendResult(resultMapper(value))
                    registerBrowsableItemUpdate(rootItemId, observable)
                },
                { throwable ->
                    resultCallback.sendErrorResult(throwable)
                    registerBrowsableItemUpdate(rootItemId, observable)
                })
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

    private fun <T> processRecentItemError(throwable: Throwable): List<T> {
        Components.getAppComponent().analytics().processNonFatalError(throwable)
        return emptyList()
    }

    private fun Result<List<MediaBrowserCompat.MediaItem>>.sendErrorResult(throwable: Throwable) {
        val errorParser = Components.getAppComponent().errorParser()
        errorParser.logError(throwable)
        val errorCommand = errorParser.parseError(throwable)
        sendErrorResult(DEFAULT_ERROR_ACTION_ID, errorCommand.message)
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

    private fun toRecentItem(playQueueEvent: PlayQueueEvent): Single<List<MediaBrowserCompat.MediaItem>> {
        val queueItem = playQueueEvent.playQueueItem ?: return Single.just(emptyList())
        val composition = queueItem.composition

        val appComponent = Components.getAppComponent()
        val coverUriSingle =
            if (appComponent.musicServiceInteractor().isCoversInNotificationEnabled) {
                appComponent.imageLoader().loadImageUri(composition)
            } else {
                Single.just(Optional(null))
            }

        return coverUriSingle.map { coverUriOpt ->
            val item = MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                    .setMediaId(RECENT_MEDIA_ACTION_ID)
                    .setTitle(formatCompositionName(composition))
                    .setSubtitle(formatCompositionAuthor(composition, this))
                    .setIconUri(coverUriOpt.value)
                    .build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
            return@map listOf(item)
        }
    }

    private fun toSearchActionItem(
        position: Int,
        composition: Composition,
        searchQuery: String?
    ) = actionItem(
        SEARCH_ITEMS_ACTION_ID,
        formatCompositionName(composition),
        formatCompositionAdditionalInfoForMediaBrowser(this, composition),
        Bundle().apply {
            putInt(POSITION_ARG, position)
            putString(SEARCH_QUERY_ARG, searchQuery)
        }
    )

    private fun toActionItem(position: Int, composition: Composition) = actionItem(
        COMPOSITIONS_ACTION_ID,
        formatCompositionName(composition),
        formatCompositionAdditionalInfoForMediaBrowser(this, composition),
        Bundle().apply { putInt(POSITION_ARG, position) }
    )

    private fun toActionItem(fileSource: FileSource, folderId: Long?): MediaBrowserCompat.MediaItem {
        return when(fileSource) {
            is FolderFileSource -> {
                browsableItem(
                    FOLDERS_NODE_ID + DELIMITER + fileSource.id,
                    fileSource.name,
                    formatCompositionsCount(this, fileSource.filesCount)
                )
            }
            is CompositionFileSource -> {
                val composition = fileSource.composition
                actionItem(
                    FOLDERS_ACTION_ID,
                    formatCompositionName(composition),
                    formatCompositionAdditionalInfoForMediaBrowser(this, composition),
                    Bundle().apply {
                        putLong(COMPOSITION_ID_ARG, composition.id)
                        putLong(FOLDER_ID_ARG, folderId ?: 0)
                    }
                )
            }
            else -> throw IllegalStateException()
        }
    }

    private fun toActionArtistItem(position: Int, composition: Composition, artistId: Long) = actionItem(
        ARTIST_ITEMS_ACTION_ID,
        formatCompositionName(composition),
        formatCompositionAdditionalInfoForMediaBrowser(this, composition),
        Bundle().apply {
            putInt(POSITION_ARG, position)
            putLong(ARTIST_ID_ARG, artistId)
        }
    )

    private fun toActionAlbumItem(position: Int, composition: Composition, albumId: Long) = actionItem(
        ALBUM_ITEMS_ACTION_ID,
        formatCompositionName(composition),
        formatCompositionAdditionalInfoForMediaBrowser(this, composition),
        Bundle().apply {
            putInt(POSITION_ARG, position)
            putLong(ALBUM_ID_ARG, albumId)
        }
    )

    private fun toActionPlaylistItem(
        position: Int,
        playlistItem: PlayListItem,
        playlistId: Long
    ): MediaBrowserCompat.MediaItem {
        val composition = playlistItem.composition
        return actionItem(
            PLAYLIST_ITEMS_ACTION_ID,
            formatCompositionName(composition),
            formatCompositionAdditionalInfoForMediaBrowser(this, composition),
            Bundle().apply {
                putInt(POSITION_ARG, position)
                putLong(PLAYLIST_ID_ARG, playlistId)
            }
        )
    }

    private fun toBrowsableItem(artist: Artist) = browsableItem(
        ARTIST_ITEMS_NODE_ID + DELIMITER + artist.id,
        artist.name,
        formatCompositionsCount(this, artist.compositionsCount)
    )

    private fun toBrowsableItem(album: Album) = browsableItem(
        ALBUM_ITEMS_NODE_ID + DELIMITER + album.id,
        album.name,
        formatAlbumAdditionalInfoForMediaBrowser(this, album)
    )

    private fun toBrowsableItem(playlist: PlayList) = browsableItem(
        PLAYLIST_ITEMS_NODE_ID + DELIMITER + playlist.id,
        playlist.name,
        formatPlayListDescriptionForMediaBrowser(this, playlist)
    )

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