/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.openfinna.android.BuildConfig;
import org.openfinna.android.R;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.utils.AuthUtils;
import org.openfinna.java.connector.FinnaClient;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.building.Building;
import org.openfinna.java.connector.interfaces.auth.AuthenticationChangeListener;

public class KirkesActivity extends AppCompatActivity implements AuthenticationChangeListener {

    public FinnaClient finnaClient;
    public LoginUser loginUser;
    public boolean loginScreen = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            loginUser = AuthUtils.getAuthentication(this);
            if (loginUser != null)
                finnaClient = new FinnaClient(loginUser.getUserAuthentication(), this);
            else
                finnaClient = new FinnaClient(this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void snack(String content, int res_id) {
        Snackbar.make(findViewById(res_id), content, 2000).show();
    }

    public void snack(String content) {
        Snackbar.make(findViewById(android.R.id.content), content, 2000).show();
    }


    public void snackReportError(Exception e) {
        final String msg = e.getMessage()!=null ? e.getMessage() : e.toString();
        Snackbar.make(findViewById(android.R.id.content), msg, 3000).setAction(R.string.report, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String mailto = "mailto:info@developerfromjokela.com" +
                        "?subject=" + Uri.encode(getString(R.string.app_name)) + " " + BuildConfig.VERSION_CODE + " error log" +
                        "&body=" + Uri.encode(msg);
                emailIntent.setData(Uri.parse(mailto));
                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    snack(getString(R.string.no_email_app));
                }
            }
        }).show();
    }

    public void snackReportError(Exception e, final int res_id) {
        final String msg = e.getMessage()!=null ? e.getMessage() : e.toString();
        Snackbar.make(findViewById(res_id), msg, 3000).setAction(R.string.report, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String mailto = "mailto:info@developerfromjokela.com" +
                        "?subject=" + Uri.encode(getString(R.string.app_name)) + " " + BuildConfig.VERSION_CODE + " error log" +
                        "&body=" + Uri.encode(msg);
                emailIntent.setData(Uri.parse(mailto));
                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    snack(getString(R.string.no_email_app), res_id);
                }
            }
        }).show();
    }

    @Override
    public void onAuthenticationChange(UserAuthentication userAuthentication, User user, Building building) {
        if (loginScreen)
            return;
        loginUser.setUserAuthentication(userAuthentication);
        if (user != null) {
            loginUser.setUser(user);
        }
        if (building != null)
            loginUser.setBuilding(building);
        try {
            AuthUtils.updateUser(loginUser, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
