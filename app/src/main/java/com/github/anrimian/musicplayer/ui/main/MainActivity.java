package com.github.anrimian.musicplayer.ui.main;

import static com.github.anrimian.musicplayer.Constants.Arguments.OPEN_PLAYER_PANEL_ARG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.utils.Permissions;
import com.github.anrimian.musicplayer.databinding.DialogErrorReportBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;
import com.github.anrimian.musicplayer.ui.player_screen.PlayerFragment;
import com.github.anrimian.musicplayer.ui.player_screen.PlayerFragmentKt;
import com.github.anrimian.musicplayer.ui.start.StartFragment;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.ViewUtils;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.FragmentUtilsKt;
import com.github.anrimian.musicplayer.utils.logger.AppLogger;
import com.github.anrimian.musicplayer.utils.logger.FileLog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            LoggerRepository loggerRepository = Components.getAppComponent().loggerRepository();
            if ((loggerRepository.wasFatalError() && loggerRepository.isReportDialogOnStartEnabled())
                    || loggerRepository.wasCriticalFatalError()) {
                FragmentUtilsKt.safeShow(new ErrorReportDialogFragment(), getSupportFragmentManager(), null);
                if (loggerRepository.wasCriticalFatalError()) {
                    return;
                }
            }

            startScreens();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Components.getAppComponent().localeController().dispatchAttachBaseContext(base));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getOpenPlayerPanelArg(intent)) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_activity_container);
            if (fragment instanceof PlayerFragment) {
                ((PlayerFragment) fragment).openPlayerPanel();//non-smooth update, why...
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_activity_container);
        if (fragment instanceof BackButtonListener && ((BackButtonListener) fragment).onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onStop() {
        super.onStop();
        AndroidUtils.hideKeyboard(getWindow().getDecorView());
    }

    private void startScreens() {
        if (Permissions.hasFilePermission(this)) {
            goToMainScreen();
        } else {
            goToStartScreen();
        }
    }

    private void goToStartScreen() {
        startFragment(new StartFragment());
    }

    private void goToMainScreen() {
        boolean openPlayQueue = getOpenPlayerPanelArg(getIntent());
        startFragment(PlayerFragmentKt.newPlayerFragment(openPlayQueue));
    }

    private void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existFragment = fragmentManager.findFragmentById(R.id.main_activity_container);
        if (existFragment == null || existFragment.getClass() != fragment.getClass()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.main_activity_container, fragment)
                    .commit();
        }
    }

    private boolean getOpenPlayerPanelArg(Intent intent) {
        boolean openPlayerPanel = intent.getBooleanExtra(OPEN_PLAYER_PANEL_ARG, false);
        getIntent().removeExtra(OPEN_PLAYER_PANEL_ARG);
        return openPlayerPanel;
    }

    public static class ErrorReportDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LoggerRepository loggerRepository = Components.getAppComponent().loggerRepository();
            boolean isCritical = loggerRepository.wasCriticalFatalError();

            DialogErrorReportBinding binding = DialogErrorReportBinding.inflate(getLayoutInflater());
            AlertDialog dialog = new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.error_report)
                    .setMessage(isCritical? R.string.critical_error_report_message: R.string.error_report_message)
                    .setView(binding.getRoot())
                    .show();

            binding.cbShowReportDialogOnStart.setVisibility(isCritical? View.GONE: View.VISIBLE);
            binding.cbShowReportDialogOnStart.setChecked(loggerRepository.isReportDialogOnStartEnabled());
            ViewUtils.onCheckChanged(binding.cbShowReportDialogOnStart, loggerRepository::showReportDialogOnStart);

            FileLog fileLog = Components.getAppComponent().fileLog();

            binding.btnDelete.setOnClickListener(v -> {
                fileLog.deleteLogFile();
                dismissAllowingStateLoss();
                onReportDialogClosed();
            });

            AppLogger appLogger = Components.getAppComponent().appLogger();

            binding.btnView.setOnClickListener(v -> appLogger.startViewLogScreen(requireActivity()));
            binding.btnSend.setOnClickListener(v -> {
                appLogger.startSendLogScreen(requireActivity());
                dismissAllowingStateLoss();
                onReportDialogClosed();
            });

            binding.btnClose.setOnClickListener(v -> {
                dismissAllowingStateLoss();
                onReportDialogClosed();
            });

            return dialog;
        }

        @Override
        public void onCancel(@NonNull DialogInterface dialog) {
            super.onCancel(dialog);
            onReportDialogClosed();
        }

        private void onReportDialogClosed() {
            AppComponent appComponent = Components.getAppComponent();
            LoggerRepository loggerRepository = appComponent.loggerRepository();
            boolean isCritical = loggerRepository.wasCriticalFatalError();
            loggerRepository.clearErrorFlags();

            if (isCritical) {
                ((MainActivity) getActivity()).startScreens();
                if (Permissions.hasFilePermission(requireContext())) {
                    appComponent.widgetUpdater().start();
                    appComponent.mediaScannerRepository().runStorageObserver();
                }
            }
        }

    }

}
