package com.github.anrimian.musicplayer.ui.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.core.content.FileProvider;

import static com.github.anrimian.musicplayer.domain.models.composition.CompositionModelHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.domain.utils.TextUtils.getLastPathSegment;

public class DialogUtils {

    public static void showConfirmDeleteDialog(Context context,
                                        List<Composition> compositions,
                                        Runnable deleteCallback) {
        String message = compositions.size() == 1?
                context.getString(R.string.delete_composition_template, formatCompositionName(compositions.get(0))):
                context.getString(R.string.delete_template, getDativCompositionsMessage(context, compositions.size()));
        showConfirmDeleteDialog(context, message, deleteCallback);
    }

    public static void showConfirmDeleteDialog(Context context,
                                               FolderFileSource folderFileSource,
                                               Runnable deleteCallback) {
        String message = context.getString(R.string.delete_folder_template,
                getLastPathSegment(folderFileSource.getFullPath()),
                getDativCompositionsMessage(context, folderFileSource.getFilesCount()));
        showConfirmDeleteDialog(context, message, deleteCallback);
    }


    public static void showConfirmDeleteDialog(Context context,
                                               PlayList playList,
                                               Runnable deleteCallback) {
        String message = context.getString(R.string.delete_playlist_template, playList.getName());
        showConfirmDeleteDialog(context, message, deleteCallback);
    }

    public static void showConfirmDeleteDialog(Context context,
                                               String message,
                                               Runnable deleteCallback) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.deleting)
                .setMessage(message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteCallback.run())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void shareFile(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");

        Uri fileUri;
        try {
            fileUri = FileProvider.getUriForFile(context,
                    context.getString(R.string.file_provider_authorities),
                    new File(filePath));
        } catch (Exception e) {
            Toast.makeText(context, R.string.file_uri_extract_error, Toast.LENGTH_SHORT).show();
            return;
        }
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
    }

    public static void shareCompositions(Context context, Collection<Composition> filePaths) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (Composition composition : filePaths) {
            uris.add(FileProvider.getUriForFile(context,
                    context.getString(R.string.file_provider_authorities),
                    new File(composition.getFilePath())));
        }
        shareFiles(context, uris);
    }

    public static void shareFiles(Context context, Collection<String> filePaths) {
        ArrayList<Uri> uris = new ArrayList<>();
        for (String path : filePaths) {
            uris.add(FileProvider.getUriForFile(context,
                    context.getString(R.string.file_provider_authorities),
                    new File(path)));
        }
        shareFiles(context, uris);
    }

    public static void shareFiles(Context context, ArrayList<Uri> uris) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("audio/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        StringBuilder sbTitle = new StringBuilder(context.getString(R.string.share));
        sbTitle.append(" (");
        sbTitle.append(context.getResources().getQuantityString(
                R.plurals.files_count,
                uris.size(),
                uris.size()));
        sbTitle.append(")");

        context.startActivity(Intent.createChooser(intent, sbTitle.toString()));
    }

    private static String getDativCompositionsMessage(Context context, int count) {
        return context.getResources().getQuantityString(R.plurals.compositions_count_dativ, count, count);
    }
}
