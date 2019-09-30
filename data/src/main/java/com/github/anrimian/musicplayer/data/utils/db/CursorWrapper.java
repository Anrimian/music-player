package com.github.anrimian.musicplayer.data.utils.db;

import android.database.Cursor;

public class CursorWrapper {

    private final Cursor cursor;

    public CursorWrapper(Cursor cursor) {
        this.cursor = cursor;
    }

    public String getString(String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    public Long getLong(String columnName) {
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }


    public Integer getInt(String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    public boolean getBoolean(String columnName) {
        return getInt(columnName) == 1;
    }
}
