package com.github.anrimian.musicplayer.ui.about;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.BuildConfig;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.utils.filelog.FileLog;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.sendEmail;

public class AboutAppFragment extends Fragment implements FragmentLayerListener {

    @BindView(R.id.container_view)
    View containerView;

    @BindView(R.id.log_actions_container)
    View logActionsContainer;

    @BindView(R.id.log_divider)
    View logDivider;

    @BindView(R.id.tv_log_info)
    TextView tvLogInfo;

    @BindView(R.id.btn_delete)
    View btnDelete;

    @BindView(R.id.btn_view)
    View btnView;

    @BindView(R.id.btn_send)
    View btnSend;

    private FileLog fileLog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);

        fileLog = Components.getAppComponent().fileLog();
        boolean isLogExists = fileLog.isFileExists();
        setLogActionsVisibility(isLogExists);
        if (isLogExists) {
            tvLogInfo.setText(getString(R.string.log_info_text, fileLog.getFileSize() / 1024));
        }

        btnDelete.setOnClickListener(v -> deleteLogFile());
        btnView.setOnClickListener(v -> startViewLogScreen());
        btnSend.setOnClickListener(v -> startSendLogScreen());

        SlidrPanel.simpleSwipeBack(containerView, this, toolbar::onStackFragmentSlided);
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(getString(R.string.version_template,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE));
        toolbar.setTitleClickListener(null);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.about_app_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_email: {
                sendEmail(requireContext(), getString(R.string.feedback_email));
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void deleteLogFile() {
        fileLog.deleteLogFile();
        setLogActionsVisibility(false);
        Toast.makeText(requireContext(), R.string.log_file_deleted, Toast.LENGTH_SHORT).show();
    }

    private void startViewLogScreen() {
        Uri uri = createUri(requireContext(), fileLog.getFile());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, "text/*");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "Text view app  not found", Toast.LENGTH_SHORT).show();
        }
    }

    //file doesn't attach
    private void startSendLogScreen() {
        Uri uri = createUri(requireContext(), fileLog.getFile());
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Log info");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ getString(R.string.log_email) });
        intent.putExtra(Intent.EXTRA_TEXT, fileLog.getLogText());
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), "Mail app not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLogActionsVisibility(boolean isLogExists) {
        int logActionsVisibility = isLogExists? VISIBLE: GONE;
        logActionsContainer.setVisibility(logActionsVisibility);
        logDivider.setVisibility(logActionsVisibility);
        tvLogInfo.setVisibility(logActionsVisibility);
    }

    private Uri createUri(Context context, File file) {
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
