/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import org.openfinna.android.R;
import org.openfinna.android.ui.main.adapters.FeesAdapter;
import org.openfinna.android.ui.utils.ErrorViewUtils;
import org.openfinna.android.ui.utils.PriceUtils;
import org.openfinna.java.connector.classes.models.fines.Fine;
import org.openfinna.java.connector.classes.models.fines.Fines;
import org.openfinna.java.connector.interfaces.FinesInterface;

import java.util.ArrayList;
import java.util.List;

public class FeesActivity extends KirkesActivity implements FinesInterface {


    private View content, error, progress, no_fees, totals_card;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeLayout;
    private TextView total, payable;
    private FeesAdapter feesAdapter;
    private List<Fine> fineList = new ArrayList<>();
    private Fines fineDetails = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fees);
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
        configureList();
        refreshFees();
    }

    private void configureViews() {
        content = findViewById(R.id.content);
        progress = findViewById(R.id.progress);
        no_fees = findViewById(R.id.no_fees_layout);
        total = findViewById(R.id.total);
        payable = findViewById(R.id.payable);
        recyclerView = findViewById(R.id.recyclerView);
        error = findViewById(R.id.errorLayout);
        swipeLayout = findViewById(R.id.swipeLayout);
        progress.setVisibility(View.VISIBLE);
        totals_card = findViewById(R.id.totals_card);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFees();
            }
        });
    }

    private void configureList() {
        feesAdapter = new FeesAdapter(fineList, fineDetails);
        recyclerView.setAdapter(feesAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(FeesActivity.this, 1);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void refreshFees() {
        finnaClient.getFines(this);
    }

    private void updateUI(Fines finesResponse) {
        no_fees.setVisibility(finesResponse.getFines().isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(finesResponse.getFines().isEmpty() ? View.GONE : View.VISIBLE);
        totals_card.setVisibility(finesResponse.getFines().isEmpty() ? View.GONE : View.VISIBLE);
        feesAdapter.setFineDetails(finesResponse);
        fineDetails = finesResponse;
        total.setText(getString(R.string.total_payment, PriceUtils.formatPrice(fineDetails.getTotalDue(), fineDetails.getCurrency())));
        payable.setText(getString(R.string.payable_amount_payment, PriceUtils.formatPrice(fineDetails.getPayableDue(), fineDetails.getCurrency())));
        fineList.clear();
        fineList.addAll(fineDetails.getFines());
        feesAdapter.notifyDataSetChanged();
    }


    @Override
    public void onFines(Fines fines) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                ErrorViewUtils.hideError(error);
                progress.setVisibility(View.GONE);
                content.setVisibility(View.VISIBLE);
                updateUI(fines);
            }
        });
    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                content.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                ErrorViewUtils.setError(e.getMessage(), error);
            }
        });
    }
}
