package com.github.anrimian.musicplayer.ui.about;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentAboutBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.AppInfo;
import com.github.anrimian.musicplayer.ui.utils.AppInfoKt;
import com.github.anrimian.musicplayer.ui.utils.ViewUtils;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.utils.logger.AppLogger;
import com.github.anrimian.musicplayer.utils.logger.FileLog;

public class AboutAppFragment extends Fragment implements FragmentLayerListener {

    private FragmentAboutBinding viewBinding;

    private FileLog fileLog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentAboutBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);

        fileLog = Components.getAppComponent().fileLog();
        AppLogger appLogger = Components.getAppComponent().appLogger();
        LoggerRepository loggerRepository = Components.getAppComponent().loggerRepository();

        boolean isLogExists = fileLog.isFileExists();
        setLogActionsVisibility(isLogExists);
        if (isLogExists) {
            viewBinding.tvLogInfo.setText(getString(R.string.log_info_text, fileLog.getFileSize() / 1024));
        }

        String aboutText = getString(R.string.about_app_text,
                linkify("mailto:", R.string.about_app_text_write, R.string.feedback_email),
                linkify("", R.string.privacy_policy, R.string.privacy_policy_link),
                linkify("mailto:", R.string.about_app_text_here, R.string.feedback_email),
                linkify("", R.string.about_app_text_here_link, R.string.source_code_link)
        );
        viewBinding.tvAbout.setText(Html.fromHtml(aboutText));
        viewBinding.tvAbout.setMovementMethod(LinkMovementMethod.getInstance());

        viewBinding.btnDelete.setOnClickListener(v -> deleteLogFile());
        viewBinding.btnView.setOnClickListener(v -> appLogger.startViewLogScreen(requireActivity()));
        viewBinding.btnSend.setOnClickListener(v -> appLogger.startSendLogScreen(requireActivity()));

        viewBinding.cbShowReportDialogOnStart.setChecked(loggerRepository.isReportDialogOnStartEnabled());
        ViewUtils.onCheckChanged(viewBinding.cbShowReportDialogOnStart, loggerRepository::showReportDialogOnStart);

        SlidrPanel.simpleSwipeBack(viewBinding.containerView, this, toolbar::onStackFragmentSlided);
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        AppInfo appInfo = AppInfoKt.getAppInfo(requireContext());
        toolbar.setSubtitle(getString(R.string.version_template,
                appInfo.getVersionName(),
                appInfo.getVersionCode()));
        toolbar.setTitleClickListener(null);
        toolbar.clearOptionsMenu();
    }

    private void deleteLogFile() {
        fileLog.deleteLogFile();
        setLogActionsVisibility(false);
        Toast.makeText(requireContext(), R.string.log_file_deleted, Toast.LENGTH_SHORT).show();
    }

    private void setLogActionsVisibility(boolean isLogExists) {
        int logActionsVisibility = isLogExists? VISIBLE: GONE;
        viewBinding.logActionsContainer.setVisibility(logActionsVisibility);
        viewBinding.tvLogInfo.setVisibility(logActionsVisibility);
    }

    private String linkify(String schema, int textResId, int linkResId) {
        return "<a href=\"" + schema + getString(linkResId) + "\">" + getString(textResId) + "</a>";
    }
}
