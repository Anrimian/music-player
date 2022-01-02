package com.github.anrimian.musicplayer.ui.common;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.github.anrimian.musicplayer.R;

import java.io.File;

public class AppAndroidUtils {

    public static Uri createUri(Context context, File file) {
        try {
            return FileProvider.getUriForFile(context,
                    context.getString(R.string.file_provider_authorities),
                    file);
        } catch (Exception e) {
            Toast.makeText(context,
                    context.getString(R.string.file_uri_extract_error, file.getPath()),
                    Toast.LENGTH_LONG).show();
            return null;
        }
    }
}
