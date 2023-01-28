package com.github.anrimian.musicplayer.data.repositories.state;

import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.CURRENT_QUEUE_ITEM_ID;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.CURRENT_QUEUE_ITEM_LAST_POSITION;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.IS_PLAYER_PANEL_OPEN;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_ALBUMS_COMPOSITIONS_POSITIONS;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_ALBUMS_POSITION;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_ALBUMS_POSITIONS_MAX_CACHE_SIZE;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_ARTISTS_COMPOSITIONS_POSITIONS;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_ARTISTS_POSITION;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_ARTISTS_POSITIONS_MAX_CACHE_SIZE;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_COMPOSITIONS_POSITION;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_FOLDERS_POSITIONS;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.LIBRARY_FOLDERS_POSITIONS_MAX_CACHE_SIZE;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.PLAYER_CONTENT_PAGE;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.PLAYLISTS_COMPOSITIONS_POSITIONS;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.PLAYLISTS_COMPOSITIONS_POSITIONS_MAX_CACHE_SIZE;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.PLAYLISTS_POSITION;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.PREFERENCES_NAME;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.SELECTED_DRAWER_SCREEN;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.SELECTED_FOLDER_SCREEN;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.SELECTED_LIBRARY_SCREEN;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.SELECTED_PLAYLIST_SCREEN;
import static com.github.anrimian.musicplayer.data.repositories.state.UiStateRepositoryImpl.Constants.TRACK_POSITION;
import static com.github.anrimian.musicplayer.domain.utils.rx.RxUtils.withDefaultValue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.collection.LruCache;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.models.Screens;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;

import javax.annotation.Nullable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Created on 16.11.2017.
 */

public class UiStateRepositoryImpl implements UiStateRepository {

    public static final long NO_ITEM = Long.MIN_VALUE;

    interface Constants {
        String PREFERENCES_NAME = "ui_preferences";

        String TRACK_POSITION = "track_position";
        String CURRENT_QUEUE_ITEM_ID = "current_play_queue_id";
        String CURRENT_QUEUE_ITEM_LAST_POSITION = "current_queue_item_last_position";
        String SELECTED_DRAWER_SCREEN = "selected_drawer_screen";
        String SELECTED_LIBRARY_SCREEN = "selected_library_screen";
        String PLAYER_CONTENT_PAGE = "player_content_page";
        String IS_PLAYER_PANEL_OPEN = "is_player_panel_open";
        String SELECTED_FOLDER_SCREEN = "selected_folder_screen_id";
        String SELECTED_PLAYLIST_SCREEN = "selected_playlist_screen";
        String LIBRARY_COMPOSITIONS_POSITION = "library_compositions_position";
        String LIBRARY_ARTISTS_POSITION = "library_artists_position";
        String LIBRARY_ALBUMS_POSITION = "library_albums_position";
        String LIBRARY_FOLDERS_POSITIONS = "library_folders_positions";
        String LIBRARY_ALBUMS_COMPOSITIONS_POSITIONS = "library_albums_compositions_positions";
        String LIBRARY_ARTISTS_COMPOSITIONS_POSITIONS = "library_artists_compositions_positions";
        String PLAYLISTS_POSITION = "playlists_positions";
        String PLAYLISTS_COMPOSITIONS_POSITIONS = "playlists_compositions_positions";
        String PLAYBACK_SPEED = "playback_speed";

        int LIBRARY_FOLDERS_POSITIONS_MAX_CACHE_SIZE = 15;
        int LIBRARY_ALBUMS_POSITIONS_MAX_CACHE_SIZE = 5;
        int LIBRARY_ARTISTS_POSITIONS_MAX_CACHE_SIZE = 5;
        int PLAYLISTS_COMPOSITIONS_POSITIONS_MAX_CACHE_SIZE = 5;
    }

    private final BehaviorSubject<Long> currentItemSubject = BehaviorSubject.create();
    private final BehaviorSubject<Float> currentPlaySpeedSubject = BehaviorSubject.create();

    private final SharedPreferencesHelper preferences;

    private final LruCachePreference foldersPositionsPreference;
    private final LruCachePreference albumsPositionsPreference;
    private final LruCachePreference artistsPositionsPreference;
    private final LruCachePreference playlistsPositionsPreference;

    public UiStateRepositoryImpl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);

        foldersPositionsPreference = new LruCachePreference(
                preferences,
                LIBRARY_FOLDERS_POSITIONS,
                LIBRARY_FOLDERS_POSITIONS_MAX_CACHE_SIZE);

        albumsPositionsPreference = new LruCachePreference(
                preferences,
                LIBRARY_ALBUMS_COMPOSITIONS_POSITIONS,
                LIBRARY_ALBUMS_POSITIONS_MAX_CACHE_SIZE);

        artistsPositionsPreference = new LruCachePreference(
                preferences,
                LIBRARY_ARTISTS_COMPOSITIONS_POSITIONS,
                LIBRARY_ARTISTS_POSITIONS_MAX_CACHE_SIZE);

        playlistsPositionsPreference = new LruCachePreference(
                preferences,
                PLAYLISTS_COMPOSITIONS_POSITIONS,
                PLAYLISTS_COMPOSITIONS_POSITIONS_MAX_CACHE_SIZE);
    }

    @Override
    public void setCurrentItemLastPosition(int position) {
        preferences.putInt(CURRENT_QUEUE_ITEM_LAST_POSITION, position);
    }

    @Override
    public int getCurrentItemLastPosition() {
        return preferences.getInt(CURRENT_QUEUE_ITEM_LAST_POSITION);
    }

    @Override
    public void setTrackPosition(long position) {
        preferences.putLong(TRACK_POSITION, position);
    }

    @Override
    public long getTrackPosition() {
        return preferences.getLong(TRACK_POSITION);
    }

    @Override
    public void setPlayerPanelOpen(boolean open) {
        preferences.putBoolean(IS_PLAYER_PANEL_OPEN, open);
    }

    @Override
    public boolean isPlayerPanelOpen() {
        return preferences.getBoolean(IS_PLAYER_PANEL_OPEN, false);
    }

    @Override
    public void setSelectedDrawerScreen(int screenId) {
        preferences.putInt(SELECTED_DRAWER_SCREEN, screenId);
    }

    @Override
    public int getSelectedDrawerScreen() {
        return preferences.getInt(SELECTED_DRAWER_SCREEN, Screens.LIBRARY);
    }

    @Override
    public void setSelectedLibraryScreen(int screenId) {
        preferences.putInt(SELECTED_LIBRARY_SCREEN, screenId);
    }

    @Override
    public int getSelectedLibraryScreen() {
        return preferences.getInt(SELECTED_LIBRARY_SCREEN, Screens.LIBRARY_COMPOSITIONS);
    }

    @Override
    public void setPlayerContentPage(int position) {
        preferences.putInt(PLAYER_CONTENT_PAGE, position);
    }

    @Override
    public int getPlayerContentPage() {
        return preferences.getInt(PLAYER_CONTENT_PAGE, 1);
    }

    @Override
    public void setSelectedFolderScreen(@Nullable Long folderId) {
        preferences.putLong(SELECTED_FOLDER_SCREEN, folderId == null? 0: folderId);
    }

    @Override
    public Long getSelectedFolderScreen() {
        long id = preferences.getLong(SELECTED_FOLDER_SCREEN);
        return id == 0? null: id;
    }

    @Override
    public void setSelectedPlayListScreenId(long playListId) {
        preferences.putLong(SELECTED_PLAYLIST_SCREEN, playListId);
    }

    @Override
    public long getSelectedPlayListScreenId() {
        return preferences.getLong(SELECTED_PLAYLIST_SCREEN);
    }

    @Override
    public void setCurrentQueueItemId(long id) {
        preferences.putLong(CURRENT_QUEUE_ITEM_ID, id);
        currentItemSubject.onNext(id);
    }

    @Override
    public Observable<Long> getCurrentItemIdObservable() {
        return withDefaultValue(currentItemSubject, this::getCurrentQueueItemId)
                .distinctUntilChanged();
    }

    @Override
    public long getCurrentQueueItemId() {
        Long cachedValue = currentItemSubject.getValue();
        if (cachedValue != null) {
            return cachedValue;
        }
        return preferences.getLong(CURRENT_QUEUE_ITEM_ID, NO_ITEM);
    }

    @Override
    public ListPosition getSavedCompositionsListPosition() {
        return preferences.getListPosition(LIBRARY_COMPOSITIONS_POSITION);
    }

    @Override
    public void saveCompositionsListPosition(ListPosition listPosition) {
        preferences.putListPosition(LIBRARY_COMPOSITIONS_POSITION, listPosition);
    }

    @Override
    public ListPosition getSavedArtistsListPosition() {
        return preferences.getListPosition(LIBRARY_ARTISTS_POSITION);
    }

    @Override
    public void saveArtistsListPosition(ListPosition listPosition) {
        preferences.putListPosition(LIBRARY_ARTISTS_POSITION, listPosition);
    }

    @Override
    public ListPosition getSavedAlbumsListPosition() {
        return preferences.getListPosition(LIBRARY_ALBUMS_POSITION);
    }

    @Override
    public void saveAlbumsListPosition(ListPosition listPosition) {
        preferences.putListPosition(LIBRARY_ALBUMS_POSITION, listPosition);
    }

    @Override
    public ListPosition getSavedPlaylistsPosition() {
        return preferences.getListPosition(PLAYLISTS_POSITION);
    }

    @Override
    public void savePlaylistsPosition(ListPosition listPosition) {
        preferences.putListPosition(PLAYLISTS_POSITION, listPosition);
    }

    @Override
    public void saveFolderListPosition(@Nullable Long id, ListPosition listPosition) {
        foldersPositionsPreference.put(id, listPosition);
    }

    @Override
    public ListPosition getSavedFolderListPosition(@Nullable Long id) {
        return foldersPositionsPreference.get(id);
    }

    @Override
    public void saveAlbumListPosition(@Nullable Long id, ListPosition listPosition) {
        albumsPositionsPreference.put(id, listPosition);
    }

    @Override
    public ListPosition getSavedAlbumListPosition(@Nullable Long id) {
        return albumsPositionsPreference.get(id);
    }

    @Override
    public void saveArtistListPosition(@Nullable Long id, ListPosition listPosition) {
        artistsPositionsPreference.put(id, listPosition);
    }

    @Override
    public ListPosition getSavedArtistListPosition(@Nullable Long id) {
        return artistsPositionsPreference.get(id);
    }

    @Override
    public void savePlaylistsListPosition(@Nullable Long id, ListPosition listPosition) {
        playlistsPositionsPreference.put(id, listPosition);
    }

    @Override
    public ListPosition getSavedPlaylistListPosition(@Nullable Long id) {
        return playlistsPositionsPreference.get(id);
    }

    @Override
    public float getCurrentPlaybackSpeed() {
        Float cachedValue = currentPlaySpeedSubject.getValue();
        if (cachedValue != null) {
            return cachedValue;
        }
        return preferences.getFloat(Constants.PLAYBACK_SPEED, 1f);
    }

    @Override
    public void setCurrentPlaybackSpeed(float speed) {
        preferences.putFloat(Constants.PLAYBACK_SPEED, speed);
        currentPlaySpeedSubject.onNext(speed);
    }

    @Override
    public Observable<Float> getPlaybackSpeedObservable() {
        return withDefaultValue(currentPlaySpeedSubject, this::getCurrentPlaybackSpeed)
                .distinctUntilChanged();
    }

    private static class LruCachePreference {

        private final SharedPreferencesHelper preferences;
        private final String preferenceKey;
        private final int cacheSize;

        private LruCache<Long, ListPosition> cachedData;

        public LruCachePreference(SharedPreferencesHelper preferences, String key, int cacheSize) {
            this.preferences = preferences;
            this.preferenceKey = key;
            this.cacheSize = cacheSize;
        }

        public void put(@Nullable Long folderId, ListPosition listPosition) {
            long key = mapToNonNull(folderId);

            LruCache<Long, ListPosition> positions = getCachedData();
            positions.put(key, listPosition);
            preferences.putLruCache(preferenceKey, positions);
        }

        @Nullable
        public ListPosition get(@Nullable Long folderId) {
            return getCachedData().get(mapToNonNull(folderId));
        }

        private LruCache<Long, ListPosition> getCachedData() {
            if (cachedData == null) {
                cachedData = preferences.getLruCache(preferenceKey, cacheSize);
            }
            return cachedData;
        }

        private long mapToNonNull(@Nullable Long key) {
            return key == null? -1L: key;
        }
    }
}
