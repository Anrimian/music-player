package com.github.anrimian.musicplayer.data.utils.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static byte[] toByteArray(InputStream is) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            byte[] buff = new byte[is.available()];
            int i;
            while ((i = is.read(buff, 0, buff.length)) > 0) {
                stream.write(buff, 0, i);
            }
            return stream.toByteArray();
        }
    }

}
