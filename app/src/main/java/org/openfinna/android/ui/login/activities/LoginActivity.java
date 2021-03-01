/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.login.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import org.openfinna.android.R;

import com.google.android.datatransport.BuildConfig;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.openfinna.android.ui.main.activities.KirkesActivity;
import org.openfinna.android.ui.main.activities.MainActivity;
import org.openfinna.android.ui.utils.AuthUtils;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.UserType;
import org.openfinna.java.connector.exceptions.InvalidCredentialsException;
import org.openfinna.java.connector.interfaces.LoginInterface;
import org.openfinna.java.connector.interfaces.UserTypeInterface;

import java.util.List;

public class LoginActivity extends KirkesActivity implements LoginInterface, UserTypeInterface {


    private Button button;
    private ProgressBar loginProgress;
    private TextInputEditText email, password;
    private boolean newAccount = false;
    private View scanCard;
    private UserType userType;
    private ArrayAdapter<UserType> userTypeArrayAdapter;
    private Spinner library;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loginScreen = true;
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null) {
            newAccount = getIntent().getBooleanExtra("newAccount", false);
            if (!newAccount) {
                if (AuthUtils.alreadyLoggedIn(this)) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
            }
        } else {
            if (AuthUtils.alreadyLoggedIn(this)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
        setContentView(R.layout.activity_login2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (newAccount)
            toolbar.setNavigationIcon(R.drawable.ic_arrow);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        button = findViewById(R.id.login_button);
        library = findViewById(R.id.library);
        loginProgress = findViewById(R.id.loginProgress);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        scanCard = findViewById(R.id.scanCode);
        scanCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(LoginActivity.this, QRScanner.class), 500);
            }
        });
        changeElementsState(false, false);
        finnaClient.getUserTypes(this);
        library.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeElementsState(true, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                changeElementsState(false, false);
            }
        });
    }

    private void login() {
        if (email.getText().length() <= 0)
            email.setError(getString(R.string.email_error));
        else {
            loginProgress.setVisibility(View.VISIBLE);
            changeElementsState(false, true);
            finnaClient.login(new UserAuthentication(((UserType)library.getSelectedItem()), email.getText().toString(), password.getText().toString()), true, this);
        }

    }


    @Override
    public void snack(String content) {
        Snackbar.make(findViewById(R.id.login_root), content, 2000).show();
    }

    @Override
    public void snackReportError(final Exception error) {
        Snackbar.make(findViewById(R.id.login_root), error.getMessage(), 3000).setAction(R.string.report, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String mailto = "mailto:info@developerfromjokela.com" +
                        "?subject=" + Uri.encode(getString(R.string.app_name)) + " " + BuildConfig.VERSION_CODE + " error log" +
                        "&body=" + Uri.encode(error.getLocalizedMessage());
                emailIntent.setData(Uri.parse(mailto));
                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    snack(getString(R.string.no_email_app));
                }
            }
        }).show();
    }


    private void changeElementsState(boolean enabled, boolean login) {
        email.setEnabled(enabled);
        password.setEnabled(enabled);
        button.setEnabled(enabled);
        library.setEnabled(login ? enabled : true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500) {
            if (resultCode == RESULT_OK) {
                assert data != null;
                email.setText(data.getStringExtra("code"));
            }
        }
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginProgress.setVisibility(View.GONE);
                boolean typesLoaded = (userTypeArrayAdapter != null && userTypeArrayAdapter.getCount()>0);
                changeElementsState(typesLoaded, typesLoaded);
                if (e instanceof InvalidCredentialsException) {
                    snack(getString(R.string.invalid_username_pass));
                } else {
                    snack(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onUserTypes(List<UserType> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userTypeArrayAdapter = new ArrayAdapter<UserType>(LoginActivity.this, android.R.layout.simple_spinner_dropdown_item);
                userTypeArrayAdapter.addAll(list);
                library.setAdapter(userTypeArrayAdapter);
            }
        });
    }

    @Override
    public void onLogin(UserAuthentication userAuthentication, User user) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginProgress.setVisibility(View.GONE);
                changeElementsState(true, true);
                try {
                    AuthUtils.saveAuthentication(userAuthentication, user, user.getBuilding(),LoginActivity.this);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    snack(e.getMessage());
                }
            }
        });
    }
}
