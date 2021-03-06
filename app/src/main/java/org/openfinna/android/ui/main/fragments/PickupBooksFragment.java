/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.openfinna.android.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.openfinna.android.classes.HomepageSavedResources;
import org.openfinna.android.classes.LoginUser;
import org.openfinna.android.ui.main.activities.BookInfoActivity;
import org.openfinna.android.ui.main.adapters.PickupBooksAdapter;
import org.openfinna.android.ui.main.adapters.ReservationAdapter;
import org.openfinna.android.ui.main.adapters.viewholders.BookHoldViewHolder;
import org.openfinna.android.ui.utils.AuthUtils;
import org.openfinna.android.ui.utils.ErrorViewUtils;
import org.openfinna.android.ui.utils.GridSpacingItemDecoration;
import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.holds.HoldingDetails;
import org.openfinna.java.connector.classes.models.holds.PickupLocation;
import org.openfinna.java.connector.classes.models.loans.Loan;
import org.openfinna.java.connector.interfaces.HoldsInterface;
import org.openfinna.java.connector.interfaces.PickupLocationsInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PickupBooksFragment extends KirkesFragment implements HoldsInterface, ReservationAdapter.BookActionsInterface {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeLayout;
    private List<Hold> books = new ArrayList<>();
    private View errorView, no_orders_layout;
    private PickupBooksAdapter adapter;
    private LoginUser loginUser;

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, null, false);
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
        no_orders_layout = view.findViewById(R.id.no_orders_layout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    loadPickupBooks();
                } catch (Exception e) {
                    swipeLayout.setRefreshing(false);
                    e.printStackTrace();
                    snack(e.getMessage());
                }
            }
        });
        adapter = new PickupBooksAdapter(this, books);

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
                no_orders_layout.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            }
        });
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, GridSpacingItemDecoration.dpToPx(10, getContext()), true));

    }

    private void loadPickupBooks() {
        ErrorViewUtils.hideError(errorView);
        finnaClient.getHolds(this);
        swipeLayout.setRefreshing(true);
    }

    @Override
    public void onGetHolds(final List<Hold> list) {
        if (getActivity() == null) return;
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeLayout.setRefreshing(false);
                ErrorViewUtils.hideError(errorView);
                recyclerView.setVisibility(View.VISIBLE);
                books.clear();
                books.addAll(list);
                Collections.sort(books, new Comparator<Hold>() {
                    @Override
                    public int compare(Hold o1, Hold o2) {
                        return o1.getHoldStatus().compareTo(o2.getHoldStatus());
                    }
                });
                Collections.sort(books, new Comparator<Hold>() {
                    @Override
                    public int compare(Hold o1, Hold o2) {
                        if (o1.getExpirationDate() != null && o2.getExpirationDate() != null)
                            return o1.getExpirationDate().compareTo(o2.getExpirationDate());
                        else
                            return 0;
                    }
                });
                adapter.notifyDataSetChanged();
                new LoadItemsTask(new LoadItemsListener() {
                    @Override
                    public void onLoad(HomepageSavedResources resources) {
                        resources.setPickupBooks(list);
                        new SaveItemsTask().execute(resources);
                    }
                }).execute();

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            loadPickupBooks();
        } catch (Exception e) {
            e.printStackTrace();
            ErrorViewUtils.setError(e.getMessage(), errorView);
        }
    }

    @Override
    public void onChangePickupLocation(Hold hold) {

    }

    @Override
    public void onMakeHold() {

    }

    @Override
    public void onCancelHold() {

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
    public void onBookClick(Hold book) {
        Intent intent = new Intent(getContext(), BookInfoActivity.class);
        intent.putExtra("resourceId", book.getId());
        startActivity(intent);
    }

    @Override
    public void onLoanClick(Loan book) {

    }


    @Override
    public void onReservationCancel(final Hold book) {
        if (getActivity() == null) return;
        @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.cancel_dialog, null);
        new BookHoldViewHolder(dialogView).onBind(book, -1, null);
        Button cancel_old = dialogView.findViewById(R.id.cancel);
        Button edit = dialogView.findViewById(R.id.edit);
        cancel_old.setVisibility(View.GONE);
        edit.setVisibility(View.GONE);
        Button cancel = dialogView.findViewById(R.id.cancelConfirm);
        Button close = dialogView.findViewById(R.id.close);

        final AlertDialog alertDialog = new MaterialAlertDialogBuilder(getContext()).setView(dialogView).show();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                final ProgressDialog progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage(getString(R.string.canceling_reservation));
                progressDialog.show();
                finnaClient.cancelHold(book, new HoldsInterface() {
                    @Override
                    public void onGetHolds(List<Hold> list) {

                    }

                    @Override
                    public void onChangePickupLocation(Hold hold) {

                    }

                    @Override
                    public void onMakeHold() {

                    }

                    @Override
                    public void onCancelHold() {
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                try {
                                    loadPickupBooks();
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
                                progressDialog.dismiss();
                                snack(e.getMessage());
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onReservationEdit(final Hold book) {
        if (getActivity() == null) return;
        @SuppressLint("InflateParams") View dialogView = getLayoutInflater().inflate(R.layout.hold_edit_dialog, null);
        final Spinner pickupSpinner = dialogView.findViewById(R.id.pickupSpinner);
        final ArrayAdapter<PickupLocation> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item);
        pickupSpinner.setAdapter(arrayAdapter);
        final ProgressBar progressBar = dialogView.findViewById(R.id.edit_progress);
        final boolean[] loaded = {false};
        final AlertDialog alertDialog = new MaterialAlertDialogBuilder(getContext()).setNegativeButton(R.string.cancel, null).setPositiveButton(R.string.save, null).setView(dialogView).show();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loaded[0]) {
                    pickupSpinner.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    PickupLocation location = (PickupLocation) pickupSpinner.getSelectedItem();
                    if (location != null) {
                        finnaClient.changeHoldPickupLocation(book, location, new HoldsInterface() {
                            @Override
                            public void onGetHolds(List<Hold> list) {

                            }

                            @Override
                            public void onChangePickupLocation(Hold hold) {
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertDialog.dismiss();
                                        snack(getString(R.string.hold_saved));
                                        try {
                                            loadPickupBooks();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            snack(e.getMessage());
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onMakeHold() {

                            }

                            @Override
                            public void onCancelHold() {

                            }

                            @Override
                            public void onError(Exception e) {
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertDialog.dismiss();
                                        snack(e.getMessage());
                                    }
                                });
                            }
                        });
                    } else {
                        alertDialog.dismiss();
                        snack(getString(R.string.pickup_location_empty));
                    }
                }
            }
        });
        finnaClient.getPickupLocations(book.getResource(), new PickupLocationsInterface() {
            @Override
            public void onFetchPickupLocations(List<PickupLocation> list, HoldingDetails holdingDetails, PickupLocation pickupLocation) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loaded[0] = true;
                        progressBar.setVisibility(View.INVISIBLE);
                        arrayAdapter.clear();
                        arrayAdapter.addAll(list);
                        arrayAdapter.notifyDataSetChanged();
                        for (PickupLocation location : list) {
                            int index = list.indexOf(location);
                            if (location.getName().toLowerCase().contains(book.getHoldPickupData().getPickupLocation().toLowerCase())) {
                                pickupSpinner.setSelection(index);
                                break;
                            }
                        }
                    }
                });
            }

            @Override
            public void onFetchDefaultPickupLocation(PickupLocation pickupLocation, List<PickupLocation> list) {

            }

            @Override
            public void onError(Exception e) {
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialog.dismiss();
                        snack(e.getMessage());
                    }
                });
            }
        }, null);
    }

    @Override
    public void onReservationRenew(Loan book) {

    }

    private class LoadItemsTask extends AsyncTask<String, HomepageSavedResources, HomepageSavedResources> {
        private LoadItemsListener loadItemsListener;

        public LoadItemsTask(LoadItemsListener loadItemsListener) {
            this.loadItemsListener = loadItemsListener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected HomepageSavedResources doInBackground(String... homepageSavedResources) {
            try {
                return AuthUtils.getHomepage(getContext());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(HomepageSavedResources s) {
            super.onPostExecute(s);
            if (getActivity() == null) return;
            if (s == null) {
                snack(getString(R.string.failed));
            } else {
                loadItemsListener.onLoad(s);
            }
        }
    }

    private class SaveItemsTask extends AsyncTask<HomepageSavedResources, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
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
