package com.github.anrimian.musicplayer.utils.logger;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;

import java.io.File;

public class AppLogger {

    private final FileLog fileLog;
    private final LoggerRepository loggerRepository;

    public AppLogger(FileLog fileLog, LoggerRepository loggerRepository) {
        this.fileLog = fileLog;
        this.loggerRepository = loggerRepository;
    }

    public void initFatalErrorRecorder() {
        Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, e) -> {
                    loggerRepository.setWasFatalError(true);
                    fileLog.writeFatalException(e);
                    if (handler != null) {
                        handler.uncaughtException(thread, e);
                    }
                });
    }

    public void startViewLogScreen(Activity activity) {
        Uri uri = createUriForFile(activity, fileLog.getFile());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "text/*");
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "Text view app  not found", Toast.LENGTH_SHORT).show();
        }
    }

    public void startSendLogScreen(Activity activity) {
        Uri uri = createUriForFile(activity, fileLog.getFile());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Log info");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("file/txt");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ activity.getString(R.string.log_email) });
        try {
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.pick_email_app_to_send)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "Mail app not found", Toast.LENGTH_SHORT).show();
        }
    }

    private static Uri createUriForFile(Context context, File file) {
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
