/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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

import org.openfinna.android.classes.HomepageSavedResources;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.main.activities.BookInfoActivity;
import org.openfinna.android.ui.main.adapters.ReservationAdapter;
import org.openfinna.android.ui.utils.AuthUtils;
import org.openfinna.android.ui.utils.ErrorViewUtils;
import org.openfinna.android.ui.utils.GridSpacingItemDecoration;
import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.loans.Loan;
import org.openfinna.java.connector.interfaces.LoansInterface;

import java.util.ArrayList;
import java.util.List;

public class ReservationsFragment extends KirkesFragment implements LoansInterface, ReservationAdapter.BookActionsInterface {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeLayout;
    private List<Loan> books = new ArrayList<>();
    private View errorView, no_reservations_view;
    private ReservationAdapter adapter;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservations, null, false);
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
        no_reservations_view = view.findViewById(R.id.no_orders_layout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    loadReservations();
                } catch (Exception e) {
                    swipeLayout.setRefreshing(false);
                    e.printStackTrace();
                }
            }
        });

        adapter = new ReservationAdapter(this, books);

        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                super.onChanged();
                checkEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                checkEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                checkEmpty();
            }

            void checkEmpty() {
                Log.e("RF", "CheckEmpty");
                no_reservations_view.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            }
        });
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, GridSpacingItemDecoration.dpToPx(10, getContext()), true));
        try {
            loadReservations();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorViewUtils.setError(e.getMessage(), errorView);
        }
    }

    private void loadReservations() {
        swipeLayout.setRefreshing(true);
        if (errorView != null)
            ErrorViewUtils.hideError(errorView);
        finnaClient.getLoans(this);
    }

    @Override
    public void onBookClick(Hold book) {

    }

    @Override
    public void onLoanClick(Loan book) {
        Intent intent = new Intent(getContext(), BookInfoActivity.class);
        intent.putExtra("book_id", book.getId());
        startActivity(intent);
    }

    @Override
    public void onReservationCancel(Hold book) {

    }

    @Override
    public void onReservationEdit(Hold book) {

    }

    @Override
    public void onGetLoans(final List<Loan> list) {
        if (getActivity() == null) return;
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                ErrorViewUtils.hideError(errorView);
                recyclerView.setVisibility(View.VISIBLE);
                books.clear();
                books.addAll(list);
                adapter.notifyDataSetChanged();
                new LoadItemsTask(new LoadItemsListener() {
                    @Override
                    public void onLoad(HomepageSavedResources resources) {
                        resources.setReservedBooks(list);
                        new SaveItemsTask().execute(resources);
                    }
                }).execute();
            }
        });
    }

    @Override
    public void onLoanRenew(Loan loan, String s) {

    }

    @Override
    public void onError(Exception e) {
        if (getActivity() == null) return;
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                e.printStackTrace();
                recyclerView.setVisibility(View.GONE);
                ErrorViewUtils.setError(e.getMessage(), errorView);
            }
        });
    }

    private interface LoadItemsListener {
        void onLoad(HomepageSavedResources resources);
    }

    @Override
    public void onReservationRenew(final Loan book) {
        if (getActivity() == null) return;
        snackWithTime(getString(R.string.renewing_please_wait, book.getResource().getTitle()), 4000);
        LoginUser user = null;
        try {
            user = AuthUtils.getAuthentication(getContext());
            assert user != null;
            finnaClient.renewLoan(book, new LoansInterface() {
                @Override
                public void onGetLoans(List<Loan> list) {

                }

                @Override
                public void onLoanRenew(Loan loan, String s) {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            snack(s);
                            try {
                                loadReservations();
                            } catch (Exception e) {
                                e.printStackTrace();
                                snack(e.getMessage());
                            }
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            e.printStackTrace();
                            snackWithTime(e.getMessage(), 3000);
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            snack(e.getMessage());
        }

    }

    private class LoadItemsTask extends AsyncTask<String, HomepageSavedResources, HomepageSavedResources> {
        private LoadItemsListener loadItemsListener;

        public LoadItemsTask(LoadItemsListener loadItemsListener) {
            this.loadItemsListener = loadItemsListener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeLayout.setRefreshing(true);
        }

        @Override
        protected HomepageSavedResources doInBackground(String... homepageSavedResources) {
            try {
                return AuthUtils.getHomepage(getContext());
            } catch (Exception e) {
                e.printStackTrace();
                snack(e.getMessage());
                return null;
            }

        }

        @Override
        protected void onPostExecute(HomepageSavedResources s) {
            if (getActivity() == null) return;
            super.onPostExecute(s);
            loadItemsListener.onLoad(s);
        }
    }

    private class SaveItemsTask extends AsyncTask<HomepageSavedResources, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeLayout.setRefreshing(false);
        }

        @Override
        protected String doInBackground(HomepageSavedResources... homepageSavedResources) {
            try {
                AuthUtils.saveHomepage(getContext(), homepageSavedResources[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
    }

}
