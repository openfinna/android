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
import org.openfinna.android.ui.main.adapters.viewholders.BibBookViewHolder;
import org.openfinna.android.ui.main.adapters.viewholders.ContinousProgressViewHolder;
import org.openfinna.java.connector.classes.ResourceInfo;

import java.util.List;

public class BibBooksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private BookActionsInterface listener;
    private List<ResourceInfo> books;
    private boolean continuous_loading = true;


    public BibBooksAdapter(BookActionsInterface listener, List<ResourceInfo> books) {
        this.listener = listener;
        this.books = books;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new BibBookViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bib_book, parent, false));
        else if (viewType == 1)
            return new ContinousProgressViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.bib_book_continous_progress, parent, false));
        else
            return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder r_holder, int position) {
        if (r_holder instanceof BibBookViewHolder) {
            BibBookViewHolder holder = (BibBookViewHolder) r_holder;
            holder.onBind(books.get(position), position, listener);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position + 1 == books.size() + 1)
            return 1;
        else
            return 0;
    }

    @Override
    public int getItemCount() {
        if (continuous_loading)
            return books.size() + 1;
        else
            return books.size();
    }

    public interface BookActionsInterface {
        void onBookClick(ResourceInfo book);

        void onReserveRequest(ResourceInfo bookInfo);
    }

    public boolean isContinuous_loading() {
        return continuous_loading;
    }

    public void setContinuous_loading(boolean continuous_loading) {
        this.continuous_loading = continuous_loading;
        notifyDataSetChanged();
    }
}
