/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.openfinna.android.R;
import com.google.android.material.textfield.TextInputEditText;

import org.openfinna.android.ui.utils.ErrorViewUtils;
import org.openfinna.android.ui.utils.OpenFinnaStorageVault;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.holds.HoldingDetails;
import org.openfinna.java.connector.classes.models.holds.PickupLocation;
import org.openfinna.java.connector.interfaces.AccountDetailsInterface;
import org.openfinna.java.connector.interfaces.PickupLocationChangeInterface;
import org.openfinna.java.connector.interfaces.PickupLocationsInterface;

import java.net.URL;
import java.util.List;
public class AccountSettingsActivity extends KirkesActivity implements AccountDetailsInterface, PickupLocationsInterface {


    private View content, error, progress;
    private SwipeRefreshLayout swipeLayout;
    private TextInputEditText full_name, firstName, lastName, address, zipCode, city, phoneNum, email, kirkes_email, nickname, defaultLib;
    private PickupLocation defaultPickupLocationResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        configureViews();
        refreshAccountDetails();
    }

    private void configureViews() {
        content = findViewById(R.id.content);
        progress = findViewById(R.id.progress);
        error = findViewById(R.id.errorLayout);
        swipeLayout = findViewById(R.id.swipeLayout);
        progress.setVisibility(View.VISIBLE);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshAccountDetails();
            }
        });

        // Initialize settings elements
        full_name = findViewById(R.id.full_name);
        firstName = findViewById(R.id.first_name);
        lastName = findViewById(R.id.last_name);
        address = findViewById(R.id.address);
        zipCode = findViewById(R.id.zipcode);
        city = findViewById(R.id.city);
        phoneNum = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        kirkes_email = findViewById(R.id.kirkes_email);
        nickname = findViewById(R.id.nickname);
        defaultLib = findViewById(R.id.default_lib);
    }


    private void refreshAccountDetails() {
        swipeLayout.setRefreshing(true);
        updateUI(loginUser.getUser());
        finnaClient.getDefaultPickupLocation(this);
        finnaClient.getAccountDetails(this);
    }

    private void changeDefaultPickupLocation(PickupLocation pickupLocation) {
        swipeLayout.setRefreshing(true);
        updateUI(loginUser.getUser());
        finnaClient.getDefaultPickupLocation(this);
        finnaClient.changeDefaultPickupLocation(pickupLocation, new PickupLocationChangeInterface() {
            @Override
            public void onPickupLocationChange(PickupLocation pickupLocation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                        defaultLib.setEnabled(true);
                        ErrorViewUtils.hideError(error);
                        progress.setVisibility(View.GONE);
                        content.setVisibility(View.VISIBLE);
                        snack(getString(R.string.default_lib_changed));
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        defaultLib.setText(defaultPickupLocationResponse.getName());
                        swipeLayout.setRefreshing(false);
                        progress.setVisibility(View.GONE);
                        AccountSettingsActivity.this.onError(e);
                    }
                });
            }
        });


    }

    private void updateUI(User account) {
        ErrorViewUtils.hideError(error);
        progress.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
        full_name.setText(account.getLibraryPreferences().getFullName());
        firstName.setText(account.getLibraryPreferences().getFirstName());
        lastName.setText(account.getLibraryPreferences().getSurname());
        address.setText(account.getLibraryPreferences().getAddress());
        zipCode.setText(account.getLibraryPreferences().getZipcode());
        city.setText(account.getLibraryPreferences().getCity());
        phoneNum.setText(account.getLibraryPreferences().getPhoneNumber());
        email.setText(account.getLibraryPreferences().getEmail());

        // Kirkes Preferences
        kirkes_email.setText(account.getKirkesPreferences().getEmail());
        nickname.setText(account.getKirkesPreferences().getNickname());

    }

    private void setDefLib(final PickupLocation defLib, final List<PickupLocation> locations) {
        defaultPickupLocationResponse = defLib;
        defaultLib.setText(defLib.getName());
        defaultLib.setEnabled(true);

        defaultLib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(AccountSettingsActivity.this, view);

                for (PickupLocation pickupLocation : locations) {
                    menu.getMenu().add(pickupLocation.getName()).setIntent(new Intent().putExtra("location", pickupLocation));
                }

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        PickupLocation selectedCourseObj = (PickupLocation) item.getIntent().getSerializableExtra("location");
                        defaultLib.setText(item.getTitle());
                        defaultLib.setEnabled(false);
                        changeDefaultPickupLocation(selectedCourseObj);
                        return false;
                    }
                });
                menu.show();
            }
        });
    }


    @Override
    public void onGetAccountDetails(User user) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                ErrorViewUtils.hideError(error);
                progress.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
                updateUI(user);
            }
        });
    }

    @Override
    public void onFetchPickupLocations(List<PickupLocation> list, HoldingDetails holdingDetails, PickupLocation pickupLocation) {

    }

    @Override
    public void onFetchDefaultPickupLocation(PickupLocation pickupLocation, List<PickupLocation> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
                setDefLib(pickupLocation, list);
            }
        });
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                ErrorViewUtils.hideError(error);
                progress.setVisibility(View.GONE);
                snack(e.getMessage());
            }
        });
    }


}
