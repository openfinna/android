/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.openfinna.android.R;

import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.main.adapters.LibraryAdapter;
import org.openfinna.java.connector.FinnaClient;
import org.openfinna.java.connector.classes.UserAuthentication;
import org.openfinna.java.connector.classes.models.User;
import org.openfinna.java.connector.classes.models.building.Building;
import org.openfinna.java.connector.classes.models.libraries.Library;
import org.openfinna.java.connector.interfaces.LibrariesInterface;
import org.openfinna.java.connector.interfaces.auth.AuthenticationChangeListener;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class SelectLibDialog implements LibrariesInterface, AuthenticationChangeListener {
    private SwipeRefreshLayout swipeLayout;
    private List<Library> libraries = new ArrayList<>();
    private View errorView;
    private LibraryAdapter adapter;
    private View view;
    private FragmentActivity context;
    private LibraryAdapter.SelectListener listener;
    private FinnaClient finnaClient;
    private LoginUser loginUser;

    public SelectLibDialog(View view, LibraryAdapter.SelectListener listener) {
        this.view = view;
        this.listener = listener;
    }

    public void init(FragmentActivity context) {
        this.context = context;
        try {
            loginUser = AuthUtils.getAuthentication(context);
            finnaClient = new FinnaClient(loginUser.getUserAuthentication(), this);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }
        RecyclerView recyclerView = view.findViewById(R.id.reservations);
        swipeLayout = view.findViewById(R.id.swipeLayout);
        errorView = view.findViewById(R.id.errorLayout);
        adapter = new LibraryAdapter(libraries, listener, finnaClient);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    loadLibraryInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                    ErrorViewUtils.setError(e.getMessage(), errorView);
                }
            }
        });
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, GridSpacingItemDecoration.dpToPx(10, getContext()), true));
        try {
            loadLibraryInfo();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorViewUtils.setError(e.getMessage(), errorView);
        }
    }

    private FragmentActivity getActivity() {
        return context;
    }

    private Context getContext() {
        return context;
    }

    private void loadLibraryInfo() {
        swipeLayout.setRefreshing(true);
        ErrorViewUtils.hideError(errorView);
        finnaClient.getLibraries(this);
    }

    @Override
    public void onGetLibraries(List<Library> list) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                libraries.clear();
                libraries.addAll(list);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onGetLibrary(Library library) {

    }

    @Override
    public void onError(Exception e) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                e.printStackTrace();
                ErrorViewUtils.setError(e.getMessage(), errorView);
            }
        });
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
            AuthUtils.updateUser(loginUser, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
