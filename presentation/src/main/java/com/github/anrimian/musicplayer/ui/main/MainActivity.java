package com.github.anrimian.musicplayer.ui.main;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.DialogErrorReportBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;
import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;
import com.github.anrimian.musicplayer.ui.player_screen.PlayerFragment;
import com.github.anrimian.musicplayer.ui.start.StartFragment;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.ViewUtils;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.utils.Permissions;
import com.github.anrimian.musicplayer.utils.logger.AppLogger;
import com.github.anrimian.musicplayer.utils.logger.FileLog;

import static com.github.anrimian.musicplayer.Constants.Arguments.OPEN_PLAY_QUEUE_ARG;

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
                showReportDialog(loggerRepository.wasCriticalFatalError());
                if (loggerRepository.wasCriticalFatalError()) {
                    return;
                }
            }

            startScreens();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (getOpenPlayQueueArg(intent)) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_activity_container);
            if (fragment instanceof PlayerFragment) {
                ((PlayerFragment) fragment).openPlayQueue();//non-smooth update, why...
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
        boolean openPlayQueue = getOpenPlayQueueArg(getIntent());
        startFragment(PlayerFragment.newInstance(openPlayQueue));
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

    private boolean getOpenPlayQueueArg(Intent intent) {
        boolean openPlayQueue = intent.getBooleanExtra(OPEN_PLAY_QUEUE_ARG, false);
        getIntent().removeExtra(OPEN_PLAY_QUEUE_ARG);
        return openPlayQueue;
    }

    private void showReportDialog(boolean isCritical) {
        LoggerRepository loggerRepository = Components.getAppComponent().loggerRepository();

        DialogErrorReportBinding binding = DialogErrorReportBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.error_report)
                .setMessage(isCritical? R.string.critical_error_report_message: R.string.error_report_message)
                .setView(binding.getRoot())
                .setOnCancelListener(o -> onReportDialogClosed(isCritical))
                .show();

        binding.cbShowReportDialogOnStart.setVisibility(isCritical? View.GONE: View.VISIBLE);
        binding.cbShowReportDialogOnStart.setChecked(loggerRepository.isReportDialogOnStartEnabled());
        ViewUtils.onCheckChanged(binding.cbShowReportDialogOnStart, loggerRepository::showReportDialogOnStart);

        FileLog fileLog = Components.getAppComponent().fileLog();

        binding.btnDelete.setOnClickListener(v -> {
            fileLog.deleteLogFile();
            dialog.dismiss();
            onReportDialogClosed(isCritical);
        });

        AppLogger appLogger = Components.getAppComponent().appLogger();

        binding.btnView.setOnClickListener(v -> appLogger.startViewLogScreen(this));
        binding.btnSend.setOnClickListener(v -> {
            appLogger.startSendLogScreen(this);
            dialog.dismiss();
            onReportDialogClosed(isCritical);
        });

        binding.btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            onReportDialogClosed(isCritical);
        });
    }

    private void onReportDialogClosed(boolean isCritical) {
        AppComponent appComponent = Components.getAppComponent();
        LoggerRepository loggerRepository = appComponent.loggerRepository();
        loggerRepository.clearErrorFlags();

        if (isCritical) {
            startScreens();
            if (Permissions.hasFilePermission(this)) {
                appComponent.widgetUpdater().start();
                appComponent.mediaScannerRepository().runStorageObserver();
            }
        }
    }

}
