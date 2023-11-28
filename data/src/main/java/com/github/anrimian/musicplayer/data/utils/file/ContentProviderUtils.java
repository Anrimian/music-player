package com.github.anrimian.musicplayer.data.utils.file;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

public class ContentProviderUtils {

    public static String getFileName(Context context, Uri uri) {
        String result;
        if (uri.getScheme().equals("content")) {
            try(Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex >= 0) {
                        return cursor.getString(columnIndex);
                    }
                }
            }
        }
        result = uri.getPath();
        int lastSeparatorIndex = result.lastIndexOf('/');
        if (lastSeparatorIndex != -1) {
            result = result.substring(lastSeparatorIndex + 1);
        }
        return result;
    }

}
