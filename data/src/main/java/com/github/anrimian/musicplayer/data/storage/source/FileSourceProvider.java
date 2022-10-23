package com.github.anrimian.musicplayer.data.storage.source;

import android.content.Context;
import android.net.Uri;

import com.github.anrimian.musicplayer.data.models.image.UriImageSource;
import com.github.anrimian.musicplayer.domain.models.image.ImageSource;
import com.github.anrimian.musicplayer.domain.utils.functions.ThrowsCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileSourceProvider {

    private static final String TEMP_FILES_FOLDER = "temp";
    private final Context context;

    public FileSourceProvider(Context context) {
        this.context = context;
    }

    public InputStream getImageStream(ImageSource imageSource) throws FileNotFoundException {
        if (imageSource instanceof UriImageSource) {
            Uri uri = ((UriImageSource) imageSource).getUri();
            return context.getContentResolver().openInputStream(uri);
        }
        throw new IllegalArgumentException("unknown image source: " + imageSource);
    }

    public long useTempFile(String name, ThrowsCallback<File> fileFunction) throws Exception {
        File file = getTempFile(name);
        try {
            fileFunction.call(file);
            return file.length();
        } finally {
            file.delete();
        }
    }

    private File getTempFile(String name) throws IOException {
        String dirPath = tempFolderPath() + File.separator + name;
        File file = new File(dirPath);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    private String tempFolderPath() {
        String folderPath = context.getFilesDir().getAbsolutePath() + File.separator + TEMP_FILES_FOLDER;
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folderPath;
    }
}
