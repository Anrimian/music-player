package com.github.anrimian.musicplayer.ui.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;

import java.util.List;

import static com.github.anrimian.musicplayer.domain.utils.TextUtils.getLastPathSegment;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionName;

public class DialogUtils {

    public static void showConfirmDeleteDialog(Context context,
                                        List<Composition> compositions,
                                        Runnable deleteCallback) {
        String message = compositions.size() == 1?
                context.getString(R.string.delete_composition_template, formatCompositionName(compositions.get(0))):
                context.getString(R.string.delete_compositions_template, compositions.size());
        showConfirmDeleteDialog(context, message, deleteCallback);
    }

    public static void showConfirmDeleteDialog(Context context,
                                               FolderFileSource folderFileSource,
                                               Runnable deleteCallback) {
        String compositionsCount = context.getResources().getQuantityString(
                R.plurals.compositions_count,
                folderFileSource.getFilesCount(),
                folderFileSource.getFilesCount());
        String message = context.getString(R.string.delete_folder_template,
                getLastPathSegment(folderFileSource.getFullPath()),
                compositionsCount);
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
}
