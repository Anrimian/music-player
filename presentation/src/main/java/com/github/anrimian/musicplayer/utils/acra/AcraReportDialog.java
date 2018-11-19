package com.github.anrimian.musicplayer.utils.acra;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.github.anrimian.musicplayer.R;

import org.acra.dialog.BaseCrashReportDialog;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created on 07.04.2017.
 */

public class AcraReportDialog extends BaseCrashReportDialog {

    @Override
    protected void init(@Nullable Bundle savedInstanceState) {
        super.init(savedInstanceState);
        setContentView(R.layout.activity_acra_report);

        TextView textView = findViewById(R.id.tv_message);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        //noinspection ThrowableResultOfMethodCallIgnored
        getException().printStackTrace(pw);
        String stackTrace = sw.toString();
        System.out.println(stackTrace);
        Log.e(getClass().getSimpleName(), stackTrace);
        textView.setText(stackTrace);

        View btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());

    }

}
