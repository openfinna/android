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

import org.openfinna.android.ui.main.adapters.viewholders.BookHoldViewHolder;
import org.openfinna.java.connector.classes.models.holds.Hold;

import java.util.List;

public class PickupBooksAdapter extends RecyclerView.Adapter<BookHoldViewHolder> {


    private ReservationAdapter.BookActionsInterface listener;
    private List<Hold> books;


    public PickupBooksAdapter(ReservationAdapter.BookActionsInterface listener, List<Hold> books) {
        this.listener = listener;
        this.books = books;
    }

    @NonNull
    @Override
    public BookHoldViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookHoldViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.book_hold, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BookHoldViewHolder holder, int position) {
        holder.onBind(books.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }
}
