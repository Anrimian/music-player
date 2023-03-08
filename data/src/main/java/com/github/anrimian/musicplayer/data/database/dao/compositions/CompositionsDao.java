package com.github.anrimian.musicplayer.data.database.dao.compositions;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.models.composition.ExternalComposition;
import com.github.anrimian.musicplayer.data.storage.providers.music.StorageComposition;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition;
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource;

import java.util.Date;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface CompositionsDao {

    @Query("SELECT " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
            "title as title, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "(SELECT name FROM artists WHERE id = (SELECT artistId FROM albums WHERE id = albumId)) as albumArtist, " +
            "trackNumber as trackNumber, " +
            "discNumber as discNumber, " +
            "comment as comment, " +
            "lyrics as lyrics, " +
            "fileName as fileName, " +
            "duration as duration, " +
            "size as size, " +
            "id as id, " +
            "storageId as storageId, " +
            "dateAdded as dateAdded, " +
            "dateModified as dateModified, " +
            "coverModifyTime as coverModifyTime, " +
            "corruptionType as corruptionType, " +
            "initialSource as initialSource " +
            "FROM compositions " +
            "WHERE id = :id " +
            "LIMIT 1")
    Observable<List<FullComposition>> getFullCompositionObservable(long id);

    @Query("SELECT IFNULL(lyrics, '') FROM compositions WHERE id = :id LIMIT 1")
    Observable<String> getLyricsObservable(long id);

    @Query("SELECT " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
            "title as title, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "(SELECT name FROM artists WHERE id = (SELECT artistId FROM albums WHERE id = albumId)) as albumArtist, " +
            "trackNumber as trackNumber, " +
            "discNumber as discNumber, " +
            "comment as comment, " +
            "lyrics as lyrics, " +
            "fileName as fileName, " +
            "duration as duration, " +
            "size as size, " +
            "id as id, " +
            "storageId as storageId, " +
            "dateAdded as dateAdded, " +
            "dateModified as dateModified, " +
            "coverModifyTime as coverModifyTime, " +
            "corruptionType as corruptionType, " +
            "initialSource as initialSource " +
            "FROM compositions " +
            "WHERE id = :id " +
            "LIMIT 1")
    FullComposition getFullComposition(long id);

    @RawQuery(observedEntities = { CompositionEntity.class, ArtistEntity.class, AlbumEntity.class })
    Observable<List<Composition>> getCompositionsObservable(SupportSQLiteQuery query);

    @RawQuery(observedEntities = { CompositionEntity.class, ArtistEntity.class, AlbumEntity.class })
    Observable<List<Composition>> getCompositionsInFolderObservable(SupportSQLiteQuery query);

    @RawQuery
    List<Composition> executeQuery(SimpleSQLiteQuery sqlQuery);

    @Query("SELECT " +
            "(" +
            "WITH RECURSIVE path(level, name, parentId) AS (" +
            "                SELECT 0, name, parentId " +
            "                FROM folders " +
            "                WHERE id = compositions.folderId " +
            "                UNION ALL " +
            "                SELECT path.level + 1, " +
            "                       folders.name, " +
            "                       folders.parentId " +
            "                FROM folders " +
            "                JOIN path ON folders.id = path.parentId " +
            "            ), " +
            "            path_from_root AS ( " +
            "                SELECT name " +
            "                FROM path " +
            "                ORDER BY level DESC " +
            "            ) " +
            "            SELECT group_concat(name, '/') " +
            "            FROM path_from_root" +
            ") AS parentPath, " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
            "title as title, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "(SELECT name FROM artists WHERE id = (SELECT artistId FROM albums WHERE id = albumId)) as albumArtist, " +
            "compositions.fileName as fileName, " +
            "compositions.duration as duration, " +
            "compositions.size as size, " +
            "compositions.id as id, " +
            "compositions.initialSource as initialSource, " +
            "compositions.storageId as storageId, " +
            "compositions.folderId as folderId, " +
            "compositions.dateAdded as dateAdded, " +
            "compositions.dateModified as dateModified, " +
            "compositions.lastScanDate as lastScanDate " +
            "FROM compositions " +
            "WHERE storageId NOTNULL " +
            "LIMIT :pageSize " +
            "OFFSET :pageIndex * :pageSize")
    List<StorageComposition> selectAllAsStorageCompositions(int pageSize, int pageIndex);

    @Insert
    long insert(CompositionEntity entity);

    @Insert
    void insert(List<CompositionEntity> entities);

    @Query("UPDATE compositions SET " +
            "title = :title, " +
            "fileName = :fileName, " +
            "duration = :duration, " +
            "size = :size, " +
            "dateModified = :dateModified " +
            "WHERE storageId = :storageId")
    void update(String title,
                String fileName,
                long duration,
                long size,
                Date dateModified,
                long storageId);

    @Query("UPDATE compositions SET " +
            "title = :title, " +
            "duration = :duration, " +
            "size = :size, " +
            "dateModified = :dateModified " +
            "WHERE id = :id")
    void update(long id,
                String title,
                long duration,
                long size,
                long dateModified);

    @Query("DELETE FROM compositions WHERE id = :id")
    void delete(long id);

    @Query("DELETE FROM compositions WHERE id in (:ids)")
    void delete(List<Long> ids);

    @Query("UPDATE compositions SET artistId = :artistId WHERE id = :id")
    void updateArtist(long id, Long artistId);

    @Query("UPDATE compositions SET albumId = :albumId WHERE id = :id")
    void updateAlbum(long id, Long albumId);

    @Query("UPDATE compositions SET title = :title WHERE id = :id")
    void updateTitle(long id, String title);

    @Query("UPDATE compositions SET duration = :duration WHERE id = :id")
    void updateDuration(long id, long duration);

    @Query("UPDATE compositions SET trackNumber = :trackNumber WHERE id = :id")
    void updateTrackNumber(long id, Long trackNumber);

    @Query("UPDATE compositions SET discNumber = :discNumber WHERE id = :id")
    void updateDiscNumber(long id, Long discNumber);

    @Query("UPDATE compositions SET comment = :comment WHERE id = :id")
    void updateComment(long id, String comment);

    @Query("UPDATE compositions SET lyrics = :lyrics WHERE id = :id")
    void updateLyrics(long id, String lyrics);

    @Query("UPDATE compositions SET size = :fileSize WHERE id = :id")
    void updateFileSize(long id, long fileSize);

    @Query("UPDATE compositions SET fileName = :fileName WHERE id = :id")
    void updateCompositionFileName(long id, String fileName);

    @Query("UPDATE compositions SET folderId = :folderId WHERE id = :id")
    void updateFolderId(long id, Long folderId);

    @Query("UPDATE compositions SET storageId = :storageId WHERE id = :id")
    void updateStorageId(long id, Long storageId);

    @Query("SELECT id FROM compositions WHERE storageId = :storageId")
    long selectIdByStorageId(long storageId);

    @Query("SELECT storageId FROM compositions WHERE id = :id")
    long selectStorageId(long id);

    @Query("SELECT storageId FROM compositions WHERE id = :id")
    Long getStorageId(long id);

    @Query("SELECT corruptionType FROM compositions WHERE id = :id")
    CorruptionType selectCorruptionType(long id);

    @Query("UPDATE compositions SET corruptionType = :corruptionType WHERE id = :id")
    void setCorruptionType(CorruptionType corruptionType, long id);

    @Query("SELECT albumId FROM compositions WHERE id = :compositionId")
    Long getAlbumId(long compositionId);

    @Query("UPDATE compositions SET albumId = :newAlbumId WHERE id = :compositionId")
    void setAlbumId(long compositionId, long newAlbumId);

    @Query("SELECT artistId FROM compositions WHERE id = :id")
    Long getArtistId(long id);

    @Query("UPDATE compositions SET dateModified = :date WHERE id = :id")
    void setUpdateTime(long id, Date date);

    @Query("UPDATE compositions SET coverModifyTime = :date, dateModified = :date, size = :size WHERE id = :id")
    void setCoverModifyTimeAndSize(long id, long size, Date date);

    @Query("UPDATE compositions SET coverModifyTime = :time WHERE id = :id")
    void setCoverModifyTime(long id, long time);

    @Query("SELECT count() FROM compositions")
    long getCompositionsCount();

    @Query("SELECT " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
            "title as title, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "(SELECT name FROM artists WHERE id = (SELECT artistId FROM albums WHERE id = albumId)) as albumArtist, " +
            "trackNumber as trackNumber, " +
            "discNumber as discNumber, " +
            "comment as comment, " +
            "lyrics as lyrics, " +
            "fileName as fileName, " +
            "duration as duration, " +
            "size as size, " +
            "id as id, " +
            "storageId as storageId, " +
            "dateAdded as dateAdded, " +
            "dateModified as dateModified, " +
            "coverModifyTime as coverModifyTime, " +
            "corruptionType as corruptionType, " +
            "initialSource as initialSource " +
            "FROM compositions " +
            "WHERE (lastScanDate < dateModified OR lastScanDate < :lastCompleteScanTime) " +
            "AND storageId IS NOT NULL " +
            "ORDER BY dateModified DESC " +
            "LIMIT :filesCount")
    Single<List<FullComposition>> selectNextCompositionsToScan(long lastCompleteScanTime, int filesCount);

    @Query("UPDATE compositions SET lastScanDate = :time WHERE id = :id")
    void setCompositionLastFileScanTime(long id, Date time);

    @Query("UPDATE compositions SET lastScanDate = 0")
    void cleanLastFileScanTime();

    @Nullable
    @Query("SELECT folderId FROM compositions WHERE id = :id")
    Long getFolderId(long id);

    @Query("SELECT " +
            "(" +
            "WITH RECURSIVE path(level, name, parentId) AS (" +
            "                SELECT 0, name, parentId " +
            "                FROM folders " +
            "                WHERE id = compositions.folderId " +
            "                UNION ALL " +
            "                SELECT path.level + 1, " +
            "                       folders.name, " +
            "                       folders.parentId " +
            "                FROM folders " +
            "                JOIN path ON folders.id = path.parentId " +
            "            ), " +
            "            path_from_root AS ( " +
            "                SELECT name " +
            "                FROM path " +
            "                ORDER BY level DESC " +
            "            ) " +
            "            SELECT group_concat(name, '/') " +
            "            FROM path_from_root" +
            ") AS parentPath, " +
            "fileName as fileName, " +
            "title as title, " +
            "(SELECT name FROM artists WHERE id = artistId) as artist, " +
            "(SELECT name FROM albums WHERE id = albumId) as album, " +
            "(SELECT name FROM artists WHERE id = (SELECT artistId FROM albums WHERE id = albumId)) as albumArtist, " +
            "trackNumber as trackNumber, " +
            "discNumber as discNumber, " +
            "comment as comment, " +
            "lyrics as lyrics, " +
            "duration as duration, " +
            "size as size, " +
            "dateAdded as dateAdded, " +
            "dateModified as dateModified, " +
            "coverModifyTime as coverModifyTime, " +
            "storageId IS NOT NULL AS isFileExists " +
            "FROM compositions ")
    List<ExternalComposition> getAllAsExternalCompositions();

    @Query("SELECT id FROM compositions WHERE fileName = :fileName ")
    long[] findCompositionsByFileName(String fileName);

    @Query("SELECT id " +
            "FROM compositions " +
            "WHERE fileName = :fileName AND (folderId = :folderId OR (folderId IS NULL AND :folderId IS NULL))")
    long findCompositionByFileName(String fileName, Long folderId);

    @Query("WITH RECURSIVE path(level, name, parentId) AS (" +
            "    SELECT 0, name, parentId" +
            "    FROM folders" +
            "    WHERE id = (SELECT folderId FROM compositions WHERE id = :id)" +
            "    UNION ALL" +
            "    SELECT path.level + 1," +
            "           folders.name," +
            "           folders.parentId" +
            "    FROM folders" +
            "    JOIN path ON folders.id = path.parentId" +
            ")," +
            "path_from_root AS (" +
            "    SELECT name" +
            "    FROM path" +
            "    ORDER BY level DESC" +
            ")" +
            "SELECT IFNULL(group_concat(name, '/'), '')" +
            "FROM path_from_root")
    String getCompositionParentPath(long id);

    @Nullable
    @Query("SELECT fileName FROM compositions WHERE id = :id")
    String getCompositionFileName(long id);

    @Query("SELECT size FROM compositions WHERE id = :id")
    long getCompositionSize(long id);

    @Query("UPDATE compositions " +
            "SET initialSource = :initialSource " +
            "WHERE id = :id AND initialSource = :updateFrom")
    void updateCompositionInitialSource(long id,
                                        InitialSource initialSource,
                                        InitialSource updateFrom);

    static StringBuilder getCompositionQuery(boolean useFileName) {
        return new StringBuilder("SELECT " +
                CompositionsDao.getCompositionSelectionQuery(useFileName) +
                "FROM compositions");
    }

    static String getCompositionSelectionQuery(boolean useFileName) {
        return "compositions.id AS id, " +
                "compositions.storageId AS storageId, " +
                "(SELECT name FROM artists WHERE id = artistId) as artist, " +
                "(SELECT name FROM albums WHERE id = albumId) as album, " +
                "(" + (useFileName? "fileName": "CASE WHEN title IS NULL OR title = '' THEN fileName ELSE title END") + ") as title, " +
                "compositions.duration AS duration, " +
                "compositions.size AS size, " +
                "compositions.dateAdded AS dateAdded, " +
                "compositions.dateModified AS dateModified, " +
                "compositions.coverModifyTime AS coverModifyTime, " +
                "storageId IS NOT NULL AS isFileExists, " +
                "initialSource AS initialSource, " +
                "compositions.corruptionType AS corruptionType ";
    }

    static StringBuilder getSearchWhereQuery(boolean useFileName) {
        StringBuilder sb = new StringBuilder(" WHERE (? IS NULL OR ");
        sb.append(useFileName? "fileName": "CASE WHEN title IS NULL OR title = '' THEN fileName ELSE title END");
        sb.append(" LIKE ? OR (artist NOTNULL AND artist LIKE ?))");
        return sb;
    }

    static StringBuilder getSearchQuery(boolean useFileName) {
        StringBuilder sb = new StringBuilder(" (? IS NULL OR ");
        sb.append(useFileName? "fileName": "CASE WHEN title IS NULL OR title = '' THEN fileName ELSE title END");
        sb.append(" LIKE ? OR (artist NOTNULL AND artist LIKE ?))");
        return sb;
    }
}
