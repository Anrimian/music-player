package com.github.anrimian.simplemusicplayer.data.utils;

import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created on 20.02.2017.
 */

public class IOUtils {

    public static void closeSilently(Cursor closeable) {
        if (closeable != null) {
            closeable.close();
        }
    }
}
