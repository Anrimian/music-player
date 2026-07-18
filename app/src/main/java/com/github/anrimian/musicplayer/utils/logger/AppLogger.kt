package com.github.anrimian.musicplayer.utils.logger;

import static com.github.anrimian.musicplayer.ui.common.AppAndroidUtils.createUri;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;
import com.github.anrimian.musicplayer.ui.utils.AppInfo;
import com.github.anrimian.musicplayer.ui.utils.AppInfoKt;

import java.util.List;

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
        Uri uri = createUri(activity, fileLog.getFile());
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
        Uri uri = createUri(activity, fileLog.getFile());
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        AppInfo appInfo = AppInfoKt.getAppInfo(activity);
        String subject = "Log info(v: " + appInfo.getVersionName() + ", build: " + appInfo.getVersionCode() + ")";
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//not working, grant manually, but leave
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ activity.getString(R.string.log_email) });

        intent.setData(Uri.parse("mailto:"));//using with Intent.ACTION_SENDTO to open email app

        //manually grant uri permission, because for stream Intent.FLAG_GRANT_READ_URI_PERMISSION flag not works
        List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : list) {
            String packageName = resolveInfo.activityInfo.packageName;
            activity.grantUriPermission(packageName, uri , Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.pick_email_app_to_send)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "Mail app not found", Toast.LENGTH_SHORT).show();
        }
    }

}
