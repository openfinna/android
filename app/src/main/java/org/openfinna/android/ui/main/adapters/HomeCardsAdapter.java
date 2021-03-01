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
import org.openfinna.android.classes.HomepageCard;
import org.openfinna.android.ui.main.adapters.viewholders.HomepageNotificationViewHolder;

import java.util.List;

public class HomeCardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<HomepageCard> books;
    private HomepageNotificationViewHolder.ActionsListener actionsListener;


    public HomeCardsAdapter(List<HomepageCard> books, HomepageNotificationViewHolder.ActionsListener actionsListener) {
        this.books = books;
        this.actionsListener = actionsListener;
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return books.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1 || viewType == 3) {
            return new HomepageNotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.homecard_notif, parent, false));
        } else if (viewType == 2)
            return new HomepageNotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.homecard_notif, parent, false));
        /*
        switch (viewType) {
            case 1:
                return new HomepageNotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.homecard_notif, parent, false));
            default:
                return null;
        }
         */
        return new HomepageNotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.homecard_notif, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 1 || holder.getItemViewType() == 3) {
            HomepageNotificationViewHolder viewHolder0 = (HomepageNotificationViewHolder) holder;
            viewHolder0.onBind(books.get(position), position, actionsListener);
        } else if (holder.getItemViewType() == 2) {
            HomepageNotificationViewHolder viewHolder0 = (HomepageNotificationViewHolder) holder;
            viewHolder0.onBind(books.get(position), position, actionsListener);
        }
        /*
         switch (holder.getItemViewType()) {
            case 1:
                HomepageNotificationViewHolder viewHolder0 = (HomepageNotificationViewHolder) holder;
                viewHolder0.onBind(books.get(position), position);
                break;

        }
         */
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

}
