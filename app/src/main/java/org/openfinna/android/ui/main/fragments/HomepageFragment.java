/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.fragments;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.openfinna.android.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.openfinna.android.classes.HomepageAction;
import org.openfinna.android.classes.HomepageCard;
import org.openfinna.android.classes.HomepageSavedResources;
import org.openfinna.android.ui.main.activities.BookInfoActivity;
import org.openfinna.android.ui.main.adapters.HomeCardsAdapter;
import org.openfinna.android.ui.main.adapters.LibraryAdapter;
import org.openfinna.android.ui.main.adapters.viewholders.HomepageNotificationViewHolder;
import org.openfinna.android.ui.utils.AuthUtils;
import org.openfinna.android.ui.utils.DateUtils;
import org.openfinna.android.ui.utils.GridSpacingItemDecoration;
import org.openfinna.android.ui.utils.LibUtils;
import org.openfinna.android.ui.utils.SelectLibDialog;
import org.openfinna.java.connector.classes.ResourceInfo;
import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.holds.HoldStatus;
import org.openfinna.java.connector.classes.models.libraries.Library;
import org.openfinna.java.connector.classes.models.libraries.schedule.Day;
import org.openfinna.java.connector.classes.models.libraries.schedule.Schedule;
import org.openfinna.java.connector.classes.models.loans.Loan;
import org.openfinna.java.connector.interfaces.HoldsInterface;
import org.openfinna.java.connector.interfaces.LibrariesInterface;
import org.openfinna.java.connector.interfaces.LoansInterface;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class HomepageFragment extends KirkesFragment implements HomepageNotificationViewHolder.ActionsListener, LibrariesInterface, HoldsInterface, LoansInterface {

    private RecyclerView recyclerView;
    private HomeCardsAdapter adapter;
    private List<HomepageCard> cards = new ArrayList<>();
    private HomepageSavedResources resources;
    private SwipeRefreshLayout refreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_homepage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            float curBrightnessValue = android.provider.Settings.System.getInt(
                    requireContext().getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
            WindowManager.LayoutParams lp = requireActivity().getWindow().getAttributes();
            lp.screenBrightness = curBrightnessValue;
            getActivity().getWindow().setAttributes(lp);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        recyclerView = view.findViewById(R.id.homepageCards);
        recyclerView.setAdapter(adapter);
        adapter = new HomeCardsAdapter(cards, this);
        recyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, GridSpacingItemDecoration.dpToPx(10, getContext()), true));
        refreshLayout = view.findViewById(R.id.swipeRefreshHome);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCards(true);
            }
        });
        new LoadItemsTask().execute();
    }

    private void refreshCards(boolean forced) {
        cards.clear();
        // Checking if available to pickup books persist
        if (resources.getPickupBooks() != null) {
            List<Hold> holds = resources.getPickupBooks();
            Collections.sort(holds, new Comparator<Hold>() {
                @Override
                public int compare(Hold o1, Hold o2) {
                    if (o1.getExpirationDate() != null && o2.getExpirationDate() != null)
                        return o1.getExpirationDate().compareTo(o2.getExpirationDate());
                    else
                        return 0;
                }
            });
            for (final Hold book : holds) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                if (book.getHoldStatus() == HoldStatus.AVAILABLE)
                    cards.add(new HomepageCard(getString(R.string.book_pickup_available), getString(R.string.book_pickup_available_details, book.getResource().getTitle(), dateFormat.format(book.getExpirationDate())), book, book.getResource().getImage(), HomepageCard.TYPE_NOTIFICATION));
            }
            if (itemsUpdateTime() || forced) {
                try {
                    refreshPickupBooks();
                } catch (Exception e) {
                    refreshLayout.setRefreshing(false);
                    e.printStackTrace();
                    snack(getString(R.string.holds_refresh_failed, e.getMessage()));
                }
            }
        } else {
            try {
                refreshPickupBooks();
            } catch (Exception e) {
                e.printStackTrace();
                refreshLayout.setRefreshing(false);
                snack(getString(R.string.holds_refresh_failed, e.getMessage()));
            }
        }
        // Checking if late or soon been due books persist
        if (resources.getReservedBooks() != null) {
            List<Loan> loans = resources.getReservedBooks();
            for (final Loan loan : loans) {
                int coD = getCountOfDays(new Date(), loan.getDueDate());
                if (coD < 0)
                    cards.add(new HomepageCard(getString(R.string.book_expired), getString(R.string.book_expired_details, loan.getResource().getTitle(), String.valueOf(Math.abs(coD))), loan, loan.getResource().getImage(), HomepageCard.TYPE_NOTIFICATION));
                else if (coD <= 7)
                    cards.add(new HomepageCard(getString(R.string.book_expire_soon), getString(R.string.book_expire_soon_details, loan.getResource().getType(), String.valueOf(coD)), loan, loan.getResource().getImage(), HomepageCard.TYPE_NOTIFICATION));

            }
            if (itemsUpdateTime() || forced) {
                try {
                    refreshReservedBooks();
                } catch (Exception e) {
                    refreshLayout.setRefreshing(false);
                    e.printStackTrace();
                    snack(getString(R.string.reservation_refresh_failed, e.getMessage()));
                }
            }
        } else {
            try {
                refreshReservedBooks();
            } catch (Exception e) {
                refreshLayout.setRefreshing(false);
                e.printStackTrace();
                snack(getString(R.string.reservation_refresh_failed, e.getMessage()));
            }
        }
        if (!resources.isLibrarySelected() && !resources.isLibraryOptOut()) {
            List<HomepageAction> actions = new ArrayList<>();
            actions.add(new HomepageAction("act_dismiss_addlib", getString(R.string.dismiss)));
            actions.add(new HomepageAction("act_addlib", getString(R.string.set_library)));
            cards.add(new HomepageCard(getString(R.string.add_library), getString(R.string.add_library_desc), R.drawable.ic_libraries, HomepageCard.TYPE_EMPTY_DUMMY, actions));
        }

        if (resources.isLibrarySelected()) {
            if (libsUpdateTime() || forced) {
                refreshLayout.setRefreshing(true);
                getLibs();
            } else
                refreshLayout.setRefreshing(false);
            updateLibraryCard();
        } else
            refreshLayout.setRefreshing(false);
        if (cards.size() < 1)
            cards.add(new HomepageCard(getString(R.string.all_set), getString(R.string.all_right), R.drawable.ic_done, HomepageCard.TYPE_EMPTY_DUMMY));

        adapter.notifyDataSetChanged();

    }

    private void getLibs() {
        finnaClient.getLibraries(this);
    }

    private void updateLibraryCard() {
        boolean libraryOpenClosed = false;
        Library library = resources.getSelectedLibrary();
        try {
            if (!LibUtils.getTodayOpenDay(library).isClosed()) {
                Date time = LibUtils.getTodayOpenDay(library).getSchedule().getCloses();
                Calendar calendar = Calendar.getInstance();
                Calendar timeCal = Calendar.getInstance();
                timeCal.setTime(time);
                calendar.setTime(LibUtils.getTodayOpenDay(library).getDate());
                calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, 0);
                libraryOpenClosed = (calendar.getTime().before(new Date()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (library != null && LibUtils.getTodayOpenDay(library) != null) {
            if (!LibUtils.getTodayOpenDay(library).isClosed() && !libraryOpenClosed) {
                if (!cards.isEmpty()) {
                    if (cards.get(0).getType() == HomepageCard.TYPE_EMPTY_DUMMY) {
                        cards.remove(0);
                        adapter.notifyItemRemoved(0);
                    }
                    if (cards.size() > 0) {
                        if (cards.get(cards.size() - 1).getType() == HomepageCard.TYPE_LIBRARY_NOTIFICATION) {
                            cards.remove(cards.size() - 1);
                            adapter.notifyItemRemoved(cards.size() - 1);
                        }
                    }
                }

                String imageUrl = "";
                if (!resources.getSelectedLibrary().getImages().isEmpty())
                    imageUrl = library.getImages().get(0).getUrl();
                cards.add(new HomepageCard(resources.getSelectedLibrary().getName(), getString(R.string.library_open_times_oncard, simpleDateFormat.format(LibUtils.getTodayOpenDay(resources.getSelectedLibrary()).getSchedule().getOpens()), simpleDateFormat.format(LibUtils.getTodayOpenDay(resources.getSelectedLibrary()).getSchedule().getCloses())), imageUrl, HomepageCard.TYPE_LIBRARY_NOTIFICATION));
                adapter.notifyItemInserted(cards.size() - 1);
            } else if (!resources.getSelectedLibrary().getDays().isEmpty()) {
                Schedule tomorrowSchedule = null;
                for (Day schedule : resources.getSelectedLibrary().getDays()) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DATE, 1);
                    if (DateUtils.sameDay(schedule.getDate(), calendar.getTime()) && !schedule.isClosed()) {
                        tomorrowSchedule = schedule.getSchedule();
                        break;
                    }
                }
                if (tomorrowSchedule != null) {
                    if (!cards.isEmpty()) {
                        if (cards.get(0).getType() == HomepageCard.TYPE_EMPTY_DUMMY) {
                            cards.remove(0);
                            adapter.notifyItemRemoved(0);
                        }
                        if (cards.get(cards.size() - 1).getType() == HomepageCard.TYPE_LIBRARY_NOTIFICATION) {
                            cards.remove(cards.size() - 1);
                            adapter.notifyItemRemoved(cards.size() - 1);
                        }
                    }


                    String imageUrl = "";
                    if (!resources.getSelectedLibrary().getImages().isEmpty())
                        imageUrl = library.getImages().get(0).getUrl();
                    cards.add(new HomepageCard(resources.getSelectedLibrary().getName(), getString(R.string.library_open_times_on_tomorrow_card, simpleDateFormat.format(tomorrowSchedule.getOpens()), simpleDateFormat.format(tomorrowSchedule.getCloses())), imageUrl, HomepageCard.TYPE_LIBRARY_NOTIFICATION));
                    adapter.notifyItemInserted(cards.size() - 1);
                }

            }
        }
    }

    private boolean libsUpdateTime() {
        try {
            Date updatedDate = AuthUtils.lastHomepageLibraryUpdated(getContext());
            if (updatedDate == null)
                return true;
            long different = new Date().getTime() - updatedDate.getTime();
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long elapsedHours = different / hoursInMilli;
            return elapsedHours > 5;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean itemsUpdateTime() {
        try {
            Date updatedDate = AuthUtils.lastHomepageItemsUpdated(getContext());
            if (updatedDate == null)
                return true;
            long different = new Date().getTime() - updatedDate.getTime();
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;
            different = different % daysInMilli;
            different = different % hoursInMilli;

            long elapsedMinutes = different / minutesInMilli;

            return elapsedMinutes > 10;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getCountOfDays(Date createdConvertedDate, Date expireCovertedDate) {


        Calendar start = new GregorianCalendar();
        start.setTime(createdConvertedDate);

        Calendar end = new GregorianCalendar();
        end.setTime(expireCovertedDate);

        long diff = end.getTimeInMillis() - start.getTimeInMillis();

        float dayCount = (float) diff / (24 * 60 * 60 * 1000);


        return (int) (dayCount);
    }

    private void refreshPickupBooks() throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        finnaClient.getHolds(this);
    }

    private void refreshReservedBooks() throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, UnsupportedEncodingException {
        finnaClient.getLoans(this);
    }

    @Override
    public void onActionClick(String actionID, final int index, HomepageCard card) {
        if (actionID.equals("act_addlib")) {
            View dialogView = getLayoutInflater().inflate(R.layout.fragment_libinfo, null);
            final AlertDialog aDialog = new MaterialAlertDialogBuilder(getContext()).setTitle(getString(R.string.select_library)).setView(dialogView).setNegativeButton(getString(R.string.cancel), null).create();
            final SelectLibDialog selDialog = new SelectLibDialog(dialogView, new LibraryAdapter.SelectListener() {
                @Override
                public void onSelect(Library library) {
                    aDialog.dismiss();
                    cards.remove(index);
                    adapter.notifyItemRemoved(index);
                    resources.setLibrarySelected(true);
                    resources.setSelectedLibrary(library);
                    new SaveItemsTask().execute(resources);
                    new LoadItemsTask().execute();
                }
            });
            aDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    selDialog.init(getActivity());
                }
            });
            aDialog.show();

        }
        if (actionID.equals("act_bk_details")) {
            Intent intent = new Intent(getContext(), BookInfoActivity.class);
            ResourceInfo book = (ResourceInfo) card.getPayload();
            intent.putExtra("resourceInfo", book);
            startActivity(intent);
        }
        if (actionID.equals("act_dismiss_addlib")) {
            cards.remove(index);
            adapter.notifyItemRemoved(index);
            resources.setLibraryOptOut(true);
            new SaveItemsTask().execute(resources);
            new LoadItemsTask().execute();
        }
    }

    @Override
    public void onGetLibraries(List<Library> list) {
        if (getActivity() == null) return;
        requireActivity().runOnUiThread(() -> {
            if (getActivity() == null) return;
            Library selectedLib = resources.getSelectedLibrary();
            for (Library library : list) {
                if (library.getId().equals(selectedLib.getId())) {
                    finnaClient.getLibrary(library, new LibrariesInterface() {
                        @Override
                        public void onGetLibraries(List<Library> list1) {

                        }

                        @Override
                        public void onGetLibrary(Library library) {
                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    resources.setSelectedLibrary(library);
                                    new SaveItemsTask().execute(resources);
                                    try {
                                        AuthUtils.updateLastHomepageLibraryUpdate(getContext());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    updateLibraryCard();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            HomepageFragment.this.onError(e);
                        }
                    });

                }
            }
        });
    }

    @Override
    public void onGetLibrary(Library library) {

    }

    @Override
    public void onGetHolds(List<Hold> list) {
        if (getActivity() == null) return;
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resources.setPickupBooks(list);
                new SaveItemsTask().execute(resources);
                try {
                    AuthUtils.updateLastHomepageItemsUpdate(getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new LoadItemsTask().execute();
            }
        });
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
    public void onGetLoans(List<Loan> list) {
        if (getActivity() == null) return;
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resources.setReservedBooks(list);
                new SaveItemsTask().execute(resources);
                try {
                    AuthUtils.updateLastHomepageItemsUpdate(getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new LoadItemsTask().execute();
            }
        });
    }

    @Override
    public void onLoanRenew(Loan loan, String s) {

    }

    @Override
    public void onError(Exception e) {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
                snack(e.getMessage());
            }
        });
    }

    private class LoadItemsTask extends AsyncTask<HomepageSavedResources, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(HomepageSavedResources... homepageSavedResources) {
            try {
                resources = AuthUtils.getHomepage(getContext());
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (getActivity() == null) return;
            refreshCards(false);
        }
    }


    private class SaveItemsTask extends AsyncTask<HomepageSavedResources, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refreshLayout.setRefreshing(true);
        }

        @Override
        protected String doInBackground(HomepageSavedResources... homepageSavedResources) {
            try {
                AuthUtils.saveHomepage(getContext(), homepageSavedResources[0]);
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (getActivity() == null) return;
            refreshLayout.setRefreshing(false);
        }
    }
}
