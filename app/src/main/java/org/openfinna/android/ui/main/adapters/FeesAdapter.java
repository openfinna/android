/*
 * Copyright (c) 2021 OpenFinna Organization. All rights reserved.
 * @author Developer From Jokela
 */

package org.openfinna.android.ui.main.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.openfinna.android.R;
import org.openfinna.android.ui.main.adapters.viewholders.FineViewHolder;
import org.openfinna.java.connector.classes.models.fines.Fine;
import org.openfinna.java.connector.classes.models.fines.Fines;

import java.util.List;

public class FeesAdapter extends RecyclerView.Adapter<FineViewHolder> {

    private List<Fine> fineList;
    private Fines fineDetails;

    public FeesAdapter(List<Fine> fineList, Fines fineDetails) {
        this.fineList = fineList;
        this.fineDetails = fineDetails;
    }

    public List<Fine> getFineList() {
        return fineList;
    }

    public void setFineList(List<Fine> fineList) {
        this.fineList = fineList;
    }

    public Fines getFineDetails() {
        return fineDetails;
    }

    public void setFineDetails(Fines fineDetails) {
        this.fineDetails = fineDetails;
    }

    @NonNull
    @Override
    public FineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FineViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fine, null));
    }

    @Override
    public void onBindViewHolder(@NonNull FineViewHolder holder, int position) {
        Fine fine = fineList.get(position);
        holder.bind(fine, fineDetails);
    }

    @Override
    public int getItemCount() {
        return fineList.size();
    }
}
