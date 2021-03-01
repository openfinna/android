/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.openfinna.android.R;

import org.openfinna.android.ui.main.adapters.viewholders.BibBookViewHolder;
import org.openfinna.android.ui.utils.ErrorViewUtils;
import org.openfinna.java.connector.classes.ResourceInfo;
import org.openfinna.java.connector.classes.models.Resource;
import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.holds.HoldingDetails;
import org.openfinna.java.connector.classes.models.holds.PickupLocation;
import org.openfinna.java.connector.interfaces.HoldsInterface;
import org.openfinna.java.connector.interfaces.PickupLocationsInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReserveActivity extends KirkesActivity implements PickupLocationsInterface, HoldsInterface {

    private View error, progress, main, subProgress;
    private ResourceInfo bookInfo;
    private Button reserveBtn;
    private EditText partText, comments, requiredBy;
    private View comment, partName, requiredDate, loanType;
    private Spinner pickupLocationSpinner, typeSpinner;
    private ArrayAdapter<PickupLocation> arrayAdapter;
    private ArrayAdapter<HoldingDetails.HoldingType> typeArrayAdapter;
    private TextView notice;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final Calendar requiredByDate = Calendar.getInstance();
    private final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);
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
        progress = findViewById(R.id.progress);
        error = findViewById(R.id.errorLayout);
        main = findViewById(R.id.reservationLayout);
        reserveBtn = findViewById(R.id.reserveBtn);
        subProgress = findViewById(R.id.reserveProgress);
        pickupLocationSpinner = findViewById(R.id.pickupSpinner);
        partText = findViewById(R.id.partText);
        comments = findViewById(R.id.commentText);
        requiredBy = findViewById(R.id.requiredByDate);
        loanType = findViewById(R.id.loanType);
        comment = findViewById(R.id.comment);
        partName = findViewById(R.id.part);
        requiredDate = findViewById(R.id.requiredBy);
        typeSpinner = findViewById(R.id.typeSpinner);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            try {
                fetchReservationDetails(bookInfo.getId());
            } catch (Exception e) {
                e.printStackTrace();
                ErrorViewUtils.setError(e.getMessage(), error);
            }
        });
        reserveBtn.setOnClickListener(v -> {
            try {
                reserve();
            } catch (Exception e) {
                e.printStackTrace();
                snack(e.getMessage());
            }
        });

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                Calendar tempC = Calendar.getInstance();
                tempC.setTime(requiredByDate.getTime());
                tempC.set(Calendar.YEAR, year);
                tempC.set(Calendar.MONTH, monthOfYear);
                tempC.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                if (tempC.before(Calendar.getInstance())) {
                    snack(getString(R.string.invalid_date));
                    return;
                }

                requiredByDate.set(Calendar.YEAR, year);
                requiredByDate.set(Calendar.MONTH, monthOfYear);
                requiredByDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        requiredBy.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    requiredBy.callOnClick();
                }
            }
        });

        requiredBy.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(ReserveActivity.this, date, requiredByDate
                        .get(Calendar.YEAR), requiredByDate.get(Calendar.MONTH),
                        requiredByDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        notice = findViewById(R.id.notice);


        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        typeArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item);
        pickupLocationSpinner.setAdapter(arrayAdapter);
        typeSpinner.setAdapter(typeArrayAdapter);


        if (intent.getExtras() != null) {
            bookInfo = (ResourceInfo) intent.getSerializableExtra("resource");
            try {
                fetchReservationDetails(bookInfo.getId());
            } catch (Exception e) {
                e.printStackTrace();
                ReserveActivity.this.onError(e);
            }
        } else
            finish();
    }

    private void updateLabel() {

        requiredBy.setText(format.format(requiredByDate.getTime()));
    }

    private void setTypeSpinnerListener() {
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                HoldingDetails.HoldingType type = typeArrayAdapter.getItem(position);
                subProgress.setVisibility(View.VISIBLE);
                changeEditElementsState(false);
                try {
                    fetchReservationDetails(bookInfo.getId(), type.getId());
                } catch (Exception e) {
                    subProgress.setVisibility(View.GONE);
                    changeEditElementsState(true);
                    snack(e.getMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void reserve() {
        changeEditElementsState(false);
        subProgress.setVisibility(View.VISIBLE);
        HoldingDetails.HoldingType reservationType = null;
        if (loanType.getVisibility() == View.VISIBLE) {
            reservationType = (HoldingDetails.HoldingType) typeSpinner.getSelectedItem();
        }
        PickupLocation pickupLocation = (PickupLocation) pickupLocationSpinner.getSelectedItem();
        Date requiredBy = null;
        if (requiredDate.getVisibility() == View.VISIBLE)
            requiredBy = requiredByDate.getTime();
        String part = null;
        if (partName.getVisibility() == View.VISIBLE)
            part = partText.getText().toString();
        String commentText = null;
        if (comment.getVisibility() == View.VISIBLE)
            commentText = comments.getText().toString();
        finnaClient.makeHold(this.bookInfo.getId(), pickupLocation, reservationType, commentText, part, requiredBy, this);
    }

    private void fetchReservationDetails(String resourceId) {
        changeEditElementsState(false);
        subProgress.setVisibility(View.VISIBLE);
        finnaClient.getPickupLocations(new Resource(resourceId), this, null);
    }

    private void fetchReservationDetails(String resourceId, String typeId) {
        changeEditElementsState(false);
        subProgress.setVisibility(View.VISIBLE);
        finnaClient.getPickupLocations(new Resource(resourceId), this, typeId);
    }

    private void setBookInfo() {
        BibBookViewHolder viewHolder = new BibBookViewHolder(findViewById(R.id.rootItem));
        viewHolder.onBind(bookInfo, -1, null);
    }

    private void changeEditElementsState(boolean enabled) {
        typeSpinner.setEnabled(enabled);
        reserveBtn.setEnabled(enabled);
        pickupLocationSpinner.setEnabled(enabled);
    }

    @Override
    public void onFetchPickupLocations(List<PickupLocation> list, HoldingDetails holdingDetails, PickupLocation pickupLocation) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.GONE);
                subProgress.setVisibility(View.GONE);
                requiredDate.setVisibility((holdingDetails.getDateSelectionValue() != null) ? View.VISIBLE : View.GONE);
                if (holdingDetails.getDateSelectionValue() != null) {
                    try {
                        Date selectionValue = format.parse(holdingDetails.getDateSelectionValue());
                        assert selectionValue != null;
                        requiredByDate.setTime(selectionValue);
                        requiredBy.setText(holdingDetails.getDateSelectionValue());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                loanType.setVisibility((!holdingDetails.getHoldingTypes().isEmpty()) ? View.VISIBLE : View.GONE);
                comment.setVisibility(holdingDetails.isCommentsEnabled() ? View.VISIBLE : View.GONE);
                partName.setVisibility(holdingDetails.isPartTextEnabled() ? View.VISIBLE : View.GONE);
                setBookInfo();
                changeEditElementsState(true);
                arrayAdapter.clear();
                arrayAdapter.addAll(list);
                arrayAdapter.notifyDataSetChanged();
                typeArrayAdapter.clear();
                typeArrayAdapter.addAll(holdingDetails.getHoldingTypes());
                typeArrayAdapter.notifyDataSetChanged();
                setTypeSpinnerListener();
                for (PickupLocation loc : list) {
                    if (loc.getId().equals(pickupLocation.getId())) {
                        pickupLocationSpinner.setSelection(list.indexOf(loc));
                        break;
                    }
                }
                notice.setText(holdingDetails.getInfo());
                main.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onFetchDefaultPickupLocation(PickupLocation pickupLocation, List<PickupLocation> list) {

    }

    @Override
    public void onGetHolds(List<Hold> list) {

    }

    @Override
    public void onChangePickupLocation(Hold hold) {

    }

    @Override
    public void onMakeHold() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeEditElementsState(true);
                subProgress.setVisibility(View.GONE);
                Toast.makeText(ReserveActivity.this, getString(R.string.reserved), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onCancelHold() {

    }

    @Override
    public void onError(Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
                subProgress.setVisibility(View.GONE);
                e.printStackTrace();
                ErrorViewUtils.setError(e.getMessage(), error);
                changeEditElementsState(true);
            }
        });
    }
}
