package com.github.anrimian.musicplayer.data.utils.db;

import android.annotation.SuppressLint;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.util.CursorUtil;

public class CursorWrapper {

    private final Cursor cursor;

    public CursorWrapper(Cursor cursor) {
        this.cursor = cursor;
    }

    @Nullable
    public String getString(String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return getString(columnIndex);
    }

    @Nullable
    public String getString(int columnIndex) {
        if (columnIndex < 0 || cursor.isNull(columnIndex)) {
            return null;
        } else {
            return cursor.getString(columnIndex);
        }
    }

    public long getLong(String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return getLong(columnIndex);
    }

    public long getLong(int columnIndex) {
        if (columnIndex < 0 || cursor.isNull(columnIndex)) {
            return 0;
        } else {
            return cursor.getLong(columnIndex);
        }
    }

    public int getInt(String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return getInt(columnIndex);
    }

    public int getInt(int columnIndex) {
        if (columnIndex < 0 || cursor.isNull(columnIndex)) {
            return 0;
        } else {
            return cursor.getInt(columnIndex);
        }
    }

    @SuppressLint("RestrictedApi")
    public static int getColumnIndex(@NonNull Cursor c, @NonNull String name) {
        return CursorUtil.getColumnIndex(c, name);
    }

    public boolean getBoolean(String columnName) {
        return getInt(columnName) == 1;
    }

    public boolean getBoolean(int columnIndex) {
        return cursor.getInt(columnIndex) == 1;
    }
}
