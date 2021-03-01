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
import org.openfinna.android.ui.main.adapters.viewholders.BookReservationViewHolder;
import org.openfinna.java.connector.classes.models.holds.Hold;
import org.openfinna.java.connector.classes.models.loans.Loan;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<BookReservationViewHolder> {


    private BookActionsInterface listener;
    private List<Loan> books;


    public ReservationAdapter(BookActionsInterface listener, List<Loan> books) {
        this.listener = listener;
        this.books = books;
    }

    @NonNull
    @Override
    public BookReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookReservationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.book_reservation, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BookReservationViewHolder holder, int position) {
        holder.onBind(books.get(position), position, listener);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public interface BookActionsInterface {
        void onBookClick(Hold book);

        void onLoanClick(Loan book);

        void onReservationCancel(Hold book);

        void onReservationEdit(Hold book);

        void onReservationRenew(Loan book);
    }
}
