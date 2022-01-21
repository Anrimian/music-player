package com.github.anrimian.musicplayer.data.storage.providers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.RequiresPermission;

import com.github.anrimian.musicplayer.data.storage.exceptions.ContentResolverQueryException;
import com.github.anrimian.musicplayer.data.storage.exceptions.UnavailableMediaStoreException;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MediaStoreUtils {

    public static void checkIfMediaStoreAvailable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Set<String> volumes = MediaStore.getExternalVolumeNames(context);
            if (!volumes.contains(MediaStore.VOLUME_EXTERNAL_PRIMARY)) {
                //can crash in rare weird cases on android 10 so we check for existence
                //https://stackoverflow.com/questions/63111091/java-lang-illegalargumentexception-volume-external-primary-not-found-in-android
                throw new UnavailableMediaStoreException();
            }
        }
    }

    @Nullable
    public static Cursor query(ContentResolver contentResolver,
                               @RequiresPermission.Read @Nonnull Uri uri,
                               @Nullable String[] projection,
                               @Nullable String selection,
                               @Nullable String[] selectionArgs,
                               @Nullable String sortOrder) {
        try {
            return contentResolver.query(uri, projection, selection, selectionArgs, sortOrder, null);
        } catch (Exception e) {
            throw new ContentResolverQueryException(e);
        }
    }

    public static boolean moveToNext(Cursor cursor) {
        try {
            return cursor.moveToNext();
        } catch (Exception e) {
            throw new ContentResolverQueryException(e);
        }
    }

}
