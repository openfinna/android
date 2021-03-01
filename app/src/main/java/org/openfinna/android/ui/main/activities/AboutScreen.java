/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import org.openfinna.android.BuildConfig;
import org.openfinna.android.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AboutScreen extends KirkesActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_about_screen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView version = findViewById(R.id.version);
        TextView compiletime = findViewById(R.id.compiled);
        Date buildDate = new Date(BuildConfig.TIMESTAMP);
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        compiletime.setText(format.format(buildDate));
        version.setText(String.format("%s (%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

        Button email = findViewById(R.id.report);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:info@developerfromjokela.com"));
                startActivity(Intent.createChooser(emailIntent, "Send feedback"));
            }
        });

    }

}
