/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.openfinna.android.R;
import org.openfinna.android.ui.main.adapters.LibraryAdapter;
import org.openfinna.android.ui.utils.ErrorViewUtils;
import org.openfinna.android.ui.utils.GridSpacingItemDecoration;
import org.openfinna.java.connector.classes.models.libraries.Library;
import org.openfinna.java.connector.interfaces.LibrariesInterface;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class LibraryInfoFragment extends KirkesFragment implements LibrariesInterface {

    private SwipeRefreshLayout swipeLayout;
    private List<Library> libraries = new ArrayList<>();
    private View errorView;
    private LibraryAdapter adapter;
    private RecyclerView recyclerView;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_libinfo, null, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            float curBrightnessValue = android.provider.Settings.System.getInt(
                    getContext().getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.screenBrightness = curBrightnessValue;
            getActivity().getWindow().setAttributes(lp);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        recyclerView = view.findViewById(R.id.reservations);
        swipeLayout = view.findViewById(R.id.swipeLayout);
        errorView = view.findViewById(R.id.errorLayout);
        adapter = new LibraryAdapter(libraries, finnaClient);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    loadLibraryInfo();
                } catch (Exception e) {
                    e.printStackTrace();
                    swipeLayout.setRefreshing(false);
                    snack(e.getMessage());
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

    private void loadLibraryInfo() throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        swipeLayout.setRefreshing(true);
        ErrorViewUtils.hideError(errorView);
        finnaClient.getLibraries(this);
    }

    @Override
    public void onGetLibraries(List<Library> list) {
        if (requireActivity() == null) return;
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                ErrorViewUtils.hideError(errorView);
                recyclerView.setVisibility(View.VISIBLE);

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
        if (getActivity() == null) return;
        swipeLayout.setRefreshing(false);
        recyclerView.setVisibility(View.GONE);
        e.printStackTrace();
        ErrorViewUtils.setError(e.getMessage(), errorView);
    }
}
