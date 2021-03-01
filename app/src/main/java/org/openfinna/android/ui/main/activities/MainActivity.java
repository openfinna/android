/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.iid.FirebaseInstanceId;

import org.openfinna.android.R;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.push.KirkesFCMPushService;
import org.openfinna.android.ui.login.activities.LoginActivity;
import org.openfinna.android.ui.main.adapters.LibraryCardsAdapter;
import org.openfinna.android.push.PushAPIUtils;
import org.openfinna.android.ui.utils.AuthUtils;
import org.openfinna.android.ui.utils.GridSpacingItemDecoration;
import org.openfinna.android.ui.utils.PriceUtils;
import org.openfinna.java.connector.classes.models.fines.Fines;
import org.openfinna.java.connector.exceptions.KirkesClientException;
import org.openfinna.java.connector.interfaces.FinesInterface;

import java.util.List;

public class MainActivity extends KirkesActivity {

    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            if (intent.hasExtra("user")) {
                LoginUser user = (LoginUser) intent.getSerializableExtra("user");
                if (user != null)
                    AuthUtils.swapUser(user, this);
            }
        }
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_reservations, R.id.navigation_orders, R.id.navigation_libinfo, R.id.navigation_libcard)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);


        sendPushToken();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setIconified(true);
                searchView.setIconified(true);
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                searchIntent.putExtra("query", query);
                startActivity(searchIntent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile) {
            //new ShowUsersDialog().execute();
            List<LoginUser> s = null;
            try {
                s = AuthUtils.getAllUsers(MainActivity.this, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (s != null) {
                try {
                    showUsersDialog(s);
                } catch (Exception e) {
                    e.printStackTrace();
                    snack(e.getMessage());
                }
            } else {
                snack(getString(R.string.user_list_failed));
            }
        }
        if (item.getItemId() == R.id.about) {
            startActivity(new Intent(MainActivity.this, AboutScreen.class));
        }
        return true;
    }

    private void showUsersDialog(final List<LoginUser> users) {
        @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.users_dialog, null, false);
        LoginUser loginUser = AuthUtils.getAuthentication(this);
        TextView userName = dialogView.findViewById(R.id.currentUsername);
        ImageView feesIcon = dialogView.findViewById(R.id.fees_icon);
        ImageView settings = dialogView.findViewById(R.id.settings);
        final ProgressBar fees_loading = dialogView.findViewById(R.id.fee_loading);
        final TextView fee_amount = dialogView.findViewById(R.id.fee_amount);
        final TextView cardNumber = dialogView.findViewById(R.id.currentCardName);
        final TextView otherCardsTitle = dialogView.findViewById(R.id.otherCardsTitle);
        assert loginUser != null;
        userName.setText(loginUser.getUser().getName());
        cardNumber.setText(loginUser.getUserAuthentication().getUsername());
        final RecyclerView otherAccounts = dialogView.findViewById(R.id.libraryCards);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(MainActivity.this, 1);
        otherAccounts.setLayoutManager(mLayoutManager);
        otherAccounts.addItemDecoration(new GridSpacingItemDecoration(1, GridSpacingItemDecoration.dpToPx(10, MainActivity.this), true));
        otherCardsTitle.setVisibility(users.isEmpty() ? View.GONE : View.VISIBLE);

        feesIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FeesActivity.class));
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AccountSettingsActivity.class));
            }
        });

        finnaClient.getFines(new FinesInterface() {
            @Override
            public void onFines(Fines fines) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fees_loading.setVisibility(View.GONE);
                                fee_amount.setVisibility(View.VISIBLE);
                                fee_amount.setText(PriceUtils.formatPrice(fines.getTotalDue(), fines.getCurrency()));
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fees_loading.setVisibility(View.GONE);
                        e.printStackTrace();
                        fee_amount.setVisibility(View.VISIBLE);
                        if (e instanceof KirkesClientException)
                            fee_amount.setText(e.getMessage());
                        else
                            fee_amount.setText(getString(R.string.error_occurred_short));
                    }
                });
            }
        });
        final AlertDialog[] dialog = new AlertDialog[1];
        final LibraryCardsAdapter cardAdapter = new LibraryCardsAdapter(new LibraryCardsAdapter.LibraryCardsActionsInterface() {
            @Override
            public void onCardClick(LoginUser user) {
                AuthUtils.swapUser(user, MainActivity.this);
                if (dialog[0] != null)
                    dialog[0].dismiss();
                recreate();
            }

            @Override
            public void onDeleteRequest(final LoginUser user, final LibraryCardsAdapter adapter) {
                new MaterialAlertDialogBuilder(MainActivity.this).setTitle(R.string.delete_card).setMessage(getString(R.string.delete_card_desc, user.getUser().getName())).setPositiveButton(R.string.delete_card, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            AuthUtils.deleteUser(user, MainActivity.this);
                            users.clear();
                            users.addAll(AuthUtils.getAllUsers(MainActivity.this, true));
                            adapter.notifyDataSetChanged();
                            otherCardsTitle.setVisibility(users.isEmpty() ? View.GONE : View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            snack(e.getMessage());
                        }
                    }
                }).setNegativeButton(R.string.cancel, null).show();
            }
        }, users);
        otherAccounts.setAdapter(cardAdapter);
        dialog[0] = new MaterialAlertDialogBuilder(MainActivity.this).setView(dialogView).setNegativeButton(R.string.logout, null).setPositiveButton(R.string.add_card, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                loginIntent.putExtra("newAccount", true);
                startActivity(loginIntent);
            }
        }).show();
        dialog[0].getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialAlertDialogBuilder(MainActivity.this).setTitle(R.string.logout).setMessage(R.string.logout_disclaimer).setPositiveButton(R.string.logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AuthUtils.removeAll(MainActivity.this);
                        finish();
                        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginIntent);
                    }
                }).setNegativeButton(R.string.cancel, null).show();
            }
        });
    }

    private class PushTokenSend extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... homepageSavedResources) {
            try {
                String token = FirebaseInstanceId.getInstance().getToken(KirkesFCMPushService.SENDER_ID, "GCM");
                Log.e(TAG, "Token: " + token);
                PushAPIUtils.getAPIService().sendPushKey(token).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private void sendPushToken() {
        new PushTokenSend().execute();
    }

}
