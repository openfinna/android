/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.openfinna.android.R;

import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.main.adapters.BibBooksAdapter;
import org.openfinna.android.ui.utils.ErrorViewUtils;
import org.openfinna.android.ui.utils.GridSpacingItemDecoration;
import org.openfinna.java.connector.classes.ResourceInfo;
import org.openfinna.java.connector.interfaces.SearchInterface;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends KirkesActivity implements SearchInterface, BibBooksAdapter.BookActionsInterface {

    private View progress;
    private RecyclerView books;
    private BibBooksAdapter adapter;
    private List<ResourceInfo> bookInfos = new ArrayList<>();
    private int page = 1;
    private View error;
    private String query = "";
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean continousLoading = false;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private boolean loadingCurrently = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        adapter = new BibBooksAdapter(this, bookInfos);
        books = findViewById(R.id.searchResult);
        books.setAdapter(adapter);
        error = findViewById(R.id.errorLayout);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        progress = findViewById(R.id.progress);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    search(query);
                } catch (Exception e) {
                    e.printStackTrace();
                    snack(e.getMessage());
                }
            }
        });
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        books.setLayoutManager(mLayoutManager);
        books.addItemDecoration(new GridSpacingItemDecoration(1, GridSpacingItemDecoration.dpToPx(10, this), true));
        if (intent != null && intent.getExtras() != null) {
            query = intent.getStringExtra("query");
            setTitle(query);
            try {
                search(query);
            } catch (Exception e) {
                e.printStackTrace();
                ErrorViewUtils.setError(e.getMessage(), error);
            }
            books.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (dy > 0) { //check for scroll down
                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                        if (continousLoading && !loadingCurrently) {
                            if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                                try {
                                    page++;
                                    Log.e("SA", "Page: " + page);
                                    loadingCurrently = true;
                                    search(query);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ErrorViewUtils.setError(e.getMessage(), error);
                                }
                                // Do pagination.. i.e. fetch new data
                            }
                        }
                    }
                }
            });
        } else
            finish();
    }

    private void search(String query) {
        swipeRefreshLayout.setEnabled(false);
        ErrorViewUtils.hideError(error);
        finnaClient.search(query, page, this);
    }

    @Override
    public void onBookClick(ResourceInfo book) {
        Intent intent = new Intent(this, BookInfoActivity.class);
        intent.putExtra("resource", book);
        startActivity(intent);
    }

    @Override
    public void onReserveRequest(ResourceInfo bookInfo) {
        Intent intent = new Intent(this, ReserveActivity.class);
        intent.putExtra("resource", bookInfo);
        startActivity(intent);
    }


    @Override
    public void onSearchResults(int i, List<ResourceInfo> list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setEnabled(true);

                progress.setVisibility(View.GONE);
                books.setVisibility(View.VISIBLE);
                if (page == 1)
                    bookInfos.clear();
                continousLoading = list.size() > 0;
                adapter.setContinuous_loading(continousLoading);
                bookInfos.addAll(list);
                adapter.notifyDataSetChanged();
                loadingCurrently = false;
            }
        });
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setEnabled(true);
                progress.setVisibility(View.GONE);
                ErrorViewUtils.setError(e.getMessage(), error);
            }
        });
    }
}
