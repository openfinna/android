/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.openfinna.android.BuildConfig;
import org.openfinna.android.R;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.main.activities.KirkesActivity;
import org.openfinna.android.ui.utils.AuthUtils;
import org.openfinna.java.OpenFinna;
import org.openfinna.java.connector.FinnaClient;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.building.Building;
import org.openfinna.java.connector.interfaces.auth.AuthenticationChangeListener;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.Objects;

public class KirkesFragment extends Fragment implements AuthenticationChangeListener {

    public FinnaClient finnaClient;
    public LoginUser loginUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            finnaClient = ((KirkesActivity)requireActivity()).finnaClient;
            if (finnaClient == null)
                finnaClient = OpenFinna.newClient();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }
    }

    public void snack(String content, int res_id) {
        if (getView() != null)
            Snackbar.make(getView().findViewById(res_id), content, 2000).show();
    }

    public void snackWithTime(String content, int time) {
        if (getActivity() != null)
            Snackbar.make(getActivity().findViewById(android.R.id.content), content, time).show();
    }


    public void snack(String content) {
        if (getActivity() != null)
            Snackbar.make(getActivity().findViewById(android.R.id.content), content, 2000).show();
    }


    public void snackReportError(final Exception e) {
        if (getActivity() != null)
            Snackbar.make(getActivity().findViewById(android.R.id.content), Objects.requireNonNull(e.getMessage()), 3000).setAction(R.string.report, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String mailto = "mailto:info@developerfromjokela.com" +
                        "?subject=" + Uri.encode(getString(R.string.app_name)) + " " + BuildConfig.VERSION_CODE + " error log" +
                        "&body=" + Uri.encode(Objects.requireNonNull(e.getMessage()));
                emailIntent.setData(Uri.parse(mailto));
                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    snack(getString(R.string.no_email_app));
                }
            }
        }).show();
    }

    public void snackReportError(final Exception e, final int res_id) {
        if (getView() != null)
            Snackbar.make(getView().findViewById(res_id), Objects.requireNonNull(e.getMessage()), 3000).setAction(R.string.report, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String mailto = "mailto:info@developerfromjokela.com" +
                        "?subject=" + Uri.encode(getString(R.string.app_name)) + " " + BuildConfig.VERSION_CODE + " error log" +
                        "&body=" + Uri.encode(Objects.requireNonNull(e.getMessage()));
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
        loginUser.setUserAuthentication(userAuthentication);
        if (user != null) {
            loginUser.setUser(user);
        }
        if (building != null)
            loginUser.setBuilding(building);
        try {
            AuthUtils.updateUser(loginUser, requireContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
